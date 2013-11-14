define([ "webjars!knockout.js", "webjars!jquery.dataTables.js", "webjars!jquery.js" ], (ko) ->
	ko.bindingHandlers.datatable = {
		init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()
			datatableOptions = allBindings.datatableOptions || {}
			
			datatableOptions.value = ko.utils.unwrapObservable(value)
			
			if !datatableOptions.name
				$.each(bindingContext.$data, (k, v) ->
					if v == value
						datatableOptions.name = k
						return false
				)
			
			$datatable = $element.dataTable(datatableOptions)
			
			if ko.isObservable(value)
				$datatable.on('save.ko', (e, params) ->
					value(params.newValue)
				)
				
			if datatableOptions.save
				$datatable.on('save', datatableOptions.save);

			ko.computed({
				read : () ->
					val = ko.utils.unwrapObservable(valueAccessor())
					if val == null
						val = ''
				, owner : this, disposeWhenNodeIsRemoved : element
			})

			if datatableOptions.visible && ko.isObservable(datatableOptions.visible)
				ko.computed({
					read : () ->
						val = ko.utils.unwrapObservable(datatableOptions.visible())
						if val
							$datatable.datatable('show')
					, owner : this,disposeWhenNodeIsRemoved : element
				})

				$datatable.on('hidden.ko', (e, params) ->
					datatableOptions.visible(false);
				)
	}
)