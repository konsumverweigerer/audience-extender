define([ "knockout", "jquery", "nv.d3", "ext/nvmodels" ], (ko) ->
  nvdddDefaults = -> {}

  cumulate = (options,data) ->
    if data? && options.cumulateOther && options.cumulateOther<data.length
      sums = data.map (n,i) -> [n.values.map((a) -> a.y).reduce(((a,b) -> a+b),0),i]
      sums.sort (a,b) -> b[0]-a[0]
      cum = {key: 'Other', cls: 'other', values: []}
      for c in sums[(options.cumulateOther)...(data.length)]
        cd = data[c[1]]
        cum.timeframe = cd.timeframe
        cum.values.push d for d in cd.values
      cum.values.sort (a,b) -> (a.x-b.x)
      cd = {}
      vals = []
      for d in cum.values
        if d.x!=cd.x
          if cd.x?
            vals.push cd
            cd = {}
          cd.x = d.x
          cd.y = d.y
        else
          cd.y += d.y
      if cd.x?
        vals.push cd
      cum.values = vals
      data = (data[c[1]] for c in sums[0...(options.cumulateOther)])
      data.push cum
    data

  rendernvddd = (element,options,data) ->
    if !$(element).is 'svg'
      element = $(element).find('svg').first()[0]
    nv.addGraph ->
      data = data || []
      chart = nv.graphs.pop()
      xformat = options.xFormat || 'date'
      yformat = options.yFormat || 'number'
      nvct = ko.unwrap options.chartType
      selection = d3.select element
      if chart? && chart.nvct!=nvct
        selection.select('*').remove()
        chart = undefined
      if nvct == 'bar'
        chart = chart || nv.models.linePlusBarChart()
      else if nvct == 'multibar'
        chart = chart || nv.models.multiBarChart()
        data = cumulate(options,data)
      else if nvct == 'cumulativeline'
        chart = chart || nv.models.mycumulativeLineChart()
        data = for n,i in data
          l = $.extend({},n)
          sv = 0
          l.values = for m,j in n.values
            d = $.extend({idx:j,series:i},m)
            d.y += sv
            sv = d.y
            d
          l
      else if nvct == 'lineplusarea'
        chart = chart || nv.models.mylinePlusStackedAreaChart()
      else if nvct == 'line'
        chart = chart || nv.models.lineChart()
        data = cumulate(options,data)
      else
        chart = chart || nv.models.lineChart()
      chart.nvct = nvct
      if xformat=='date'
        minx = -1
        maxx = -1
        for n,i in data
          for m in n.values
            if minx == -1 || m.x<minx
              minx = m.x
            if maxx == -1 || m.x>maxx
              maxx = m.x
        if ((maxx-minx)/(24*60*60*1000))<2
          xformat = 'time'
        if chart.lines
          chart.lines.xScale d3.time.scale()
        chart.xAxis.scale d3.time.scale()
      chart.xAxis.showMaxMin(false).staggerLabels(true).tickFormat (d) ->
        if xformat=='date'
          d3.time.format('%m/%d/%Y')(new Date d)
        else if xformat=='time'
          d3.time.format('%H:%M:%S')(new Date d)
        else
          (d3.format '.2f') d
      chart.yAxis.showMaxMin(false).tickFormat (d) ->
        if yformat=='currency'
          '$'+(d3.format '.2f') d
        else if yformat=='integer'
          (d3.format '.0f') d
        else
          (d3.format '.2f') d
      for opt in ['color','showLegend','width','height','tooltips','tooltipContent']
        if options[opt]? && chart[opt]?
          chart[opt] options[opt]
      selection.datum data
      selection.transition().duration(500).call chart
      nv.utils.windowResize ->
        c?.update() for c in nv.graphs
      return chart
  ko.bindingHandlers.nvddd =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
      nvdddOptions = $.extend(nvdddDefaults(),allBindings.nvdddOptions || {})
      if nvdddOptions.chartType? && ko.isObservable nvdddOptions.chartType
        nvdddOptions.chartType.subscribe (nv) ->
          ko.bindingHandlers.nvddd.update(element,valueAccessor,allBindingsAccessor,viewModel,bindingContext)
      if val.chartcontent? && ko.isObservable val.chartcontent
        val.chartcontent.subscribe (nv) ->
          ko.bindingHandlers.nvddd.update(element,val.chartcontent,allBindingsAccessor,viewModel,bindingContext)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
      nvdddOptions = $.extend(nvdddDefaults(),allBindings.nvdddOptions || {})
      if val == null
        val = []
      rendernvddd(element,nvdddOptions,val)
)