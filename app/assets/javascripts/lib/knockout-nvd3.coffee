define([ "webjars!knockout.js", "webjars!d3.v2.js", "webjars!jquery.js", "webjars!nv.d3.js", "ext/nvmodels"], (ko) ->
  rendernvddd = (element, options, data) ->
    if !$(element).is('svg')
      element = $(element).find('svg').first()[0]
    nv.graphs.pop()
    nv.addGraph(() ->
      xformat = options.xFormat || 'date'
      yformat = options.yFormat || 'number'
      if options.chartType == 'bar'
        chart = window.chart || nv.models.linePlusBarChart()
      else if options.chartType == 'multibar'
        chart = window.chart || nv.models.multiBarChart()
        if options.cumulateOther
          sums = data.map((n,i) ->
            [i, n.values.reduce((a,b) ->
              a+b
            , 0)]
          )
      else if options.chartType == 'cumulativeline'
        chart = window.chart || nv.models.mycumulativeLineChart()
        data = data.map((n,i) ->
          l = $.extend({},n)
          sv = 0
          l.values = n.values.map((m,j) ->
            d = $.extend({idx:j,series:i},m)
            d.y += sv
            sv = d.y
            d
          )
          l
        )
      else
        chart = window.chart || nv.models.lineChart()
      if xformat=='date'
        minx = -1
        maxx = -1
        data.filter((n,i) ->
          n.values.filter((m,j) ->
            if minx == -1 || m.x<minx
              minx = m.x
            if maxx == -1 || m.x>maxx
              maxx = m.x
            false
          )
          false
        )
        if ((maxx-minx)/(24*60*60*1000))<2
          xformat = 'time'
        if chart.lines
          chart.lines.xScale(d3.time.scale())
        chart.xAxis.scale(d3.time.scale())
      chart.xAxis.showMaxMin(false).staggerLabels(true)
        .tickFormat((d) ->
          if xformat=='date'
            d3.time.format('%m/%d/%Y')(new Date(d))
          else if xformat=='time'
            d3.time.format('%H:%M:%S')(new Date(d))
          else
            (d3.format('.2f'))(d)
        )
      chart.yAxis.showMaxMin(false)
        .tickFormat((d) ->
          if yformat=='currency'
            '$'+(d3.format('.2f'))(d)
          else if yformat=='integer'
            (d3.format('.0f'))(d)
          else
            (d3.format('.2f'))(d)
        )
      if options.colors
        chart.color(options.colors)
      d3.select(element)
        .datum(data)
        .transition().duration(500).call(chart)
      chart.data = data
      window.chart = chart
      nv.utils.windowResize(chart.update)
    )
  ko.bindingHandlers.nvddd = {
    init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
      nvdddOptions = allBindings.nvdddOptions || {}
      if val.chartcontent && ko.isObservable(val.chartcontent)
        val.chartcontent.subscribe( (nv) ->
          ko.bindingHandlers.datatable.update(element, val.chartcontent, allBindingsAccessor, viewModel, bindingContext)
        )
    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
      nvdddOptions = allBindings.nvdddOptions || {}
      if val == null
        val = []
      rendernvddd(element,nvdddOptions,val)
  })