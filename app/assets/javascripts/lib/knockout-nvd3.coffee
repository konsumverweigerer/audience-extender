define([ "webjars!knockout.js", "webjars!d3.v2.js", "webjars!jquery.js" ], (ko) ->
 	require(["webjars!nv.d3.js"], () ->
		ko.bindingHandlers.datatable = {
			init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
				$element = $(element)
				value = valueAccessor()
				allBindings = allBindingsAccessor()
				nvdddOptions = allBindings.nvdddOptions || {}

				$nvddd = $element.dataTable(nvdddOptions)

			, update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
				$element = $(element)
				value = valueAccessor()
				allBindings = allBindingsAccessor()

				$datatable = $element.dataTable()

				val = ko.utils.unwrapObservable(valueAccessor())
				if val == null
					val = [];
				$datatable.fnClearTable()
				i = 0
				while i++ < val.length
					$datatable.fnAddData(val[i])
		}
	)
)