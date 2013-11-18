define([ "webjars!knockout.js", "webjars!d3.v2.js", "webjars!jquery.js" ], (ko) ->
  require(["webjars!nv.d3.js"], () ->
    ko.bindingHandlers.datatable = {
      init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
        $element = $(element)
        val = valueAccessor()
        allBindings = allBindingsAccessor()
        nvdddOptions = allBindings.nvdddOptions || {}
        if val == null
          val = []
      , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
        $element = $(element)
        value = valueAccessor()
        allBindings = allBindingsAccessor()
        nvdddOptions = allBindings.nvdddOptions || {}
        val = valueAccessor()
        if val == null
          val = []
    }))
