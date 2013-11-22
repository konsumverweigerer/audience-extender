define([ "webjars!knockout.js", "webjars!d3.v2.js", "webjars!nv.d3.js"], (ko) ->
  rendernvddd = (element, options, data) ->
    if !$(element).is('svg')
      element = $(element).find('svg').first()[0]
    nv.graphs.pop()
    nv.addGraph(() ->
      xformat = options.xFormat || 'date'
      yformat = options.yFormat || 'number'
      if options.chartType == 'bar'
        chart = nv.models.linePlusBarChart()
      else if options.chartType == 'multibar'
        chart = nv.models.multiBarChart()
      else if options.chartType == 'cumulativeline'
        chart = nv.models.cumulativeLineChart()
      else
        chart = nv.models.lineChart()
      chart.xAxis.showMaxMin(false).staggerLabels(true)
        .tickFormat((d) ->
          if xformat=='date'
            d3.time.format('%m/%d/%Y')(new Date(d))
          else if xformat=='time'
            d3.time.format('%H/%M/%S')(new Date(d))
          else
            (d3.format('.2f'))(d)
        )
      chart.yAxis.showMaxMin(false)
        .tickFormat((d) ->
          if yformat=='currency'
            '$'+(d3.format('.2f'))(d)
          if yformat=='integer'
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