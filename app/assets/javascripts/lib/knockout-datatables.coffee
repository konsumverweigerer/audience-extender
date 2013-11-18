define([ "webjars!knockout.js", "webjars!jquery.dataTables.js", "webjars!jquery.js" ], (ko) ->
	ko.bindingHandlers.datatable = {
		init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()
			datatableOptions = allBindings.datatableOptions || {}

			$datatable = $element.dataTable(datatableOptions)

		, update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()

			$datatable = $element.dataTable()

			val = ko.utils.unwrapObservable(valueAccessor())
			if val == null
				val = []
			$datatable.fnClearTable()
			i = 0
			while i++ < val.length
				$datatable.fnAddData(val[i])
	}
)