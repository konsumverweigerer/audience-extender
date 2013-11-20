define([ "webjars!knockout.js", "webjars!d3.v2.js" ], (ko) ->
  require(["webjars!nv.d3.js"], () ->
    rendernvddd = (element, options, data) ->
      nv.graphs.pop()
      nv.addGraph(() ->
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
            d3.time.format(models.chartdaterange.format())(new Date(d))
          )
        chart.yAxis.showMaxMin(false)
          .tickFormat((d) ->
            '$'+(d3.format('.2f'))(d)
          )
        if options.colors
          chart.color(options.colors)
        d3.select(element)
          .datum(data)
          .transition().duration(500).call(chart)
        nv.utils.windowResize(chart.update)
        return chart
      )
    ko.bindingHandlers.nvddd = {
      init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
        val = valueAccessor()
        allBindings = allBindingsAccessor()
        nvdddOptions = allBindings.nvdddOptions || {}

        if val == null
          val = []
        rendernvddd(element,nvdddOptions,val)
      , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
        val = valueAccessor()
        allBindings = allBindingsAccessor()
        nvdddOptions = allBindings.nvdddOptions || {}

        if val == null
          val = []
        rendernvddd(element,nvdddOptions,val)
    }))
