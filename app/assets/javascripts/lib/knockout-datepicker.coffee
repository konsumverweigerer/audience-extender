define([ "webjars!knockout.js", "webjars!bootstrap-datepicker.js", "webjars!jquery.js" ], (ko) ->
	ko.bindingHandlers.datepicker = {
		init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()
			datepickerOptions = allBindings.datepickerOptions || {}

			datepickerOptions.value = ko.utils.unwrapObservable(value)

			if !datepickerOptions.name
				$.each(bindingContext.$data, (k, v) ->
					if v == value
						datepickerOptions.name = k
						return false
				)

			if !datepickerOptions.validate && value.isValid
				datepickerOptions.validate = (testValue) ->
					initalValue = value()
					value(testValue)
					res = value.isValid() ? null : ko.utils.unwrapObservable(value.error)
					value(initalValue)
					return res

			$datepicker = $element.datepicker(datepickerOptions)

			if ko.isObservable(value)
				$datepicker.on('save.ko', (e, params) ->
					value(params.newValue)
				)

			if datepickerOptions.save
				$datepicker.on('save', datepickerOptions.save)

			ko.computed({
				read : () ->
					val = ko.utils.unwrapObservable(valueAccessor())
					if val == null
						val = ''
					$datepicker.datepicker('setDate', val, true)
				, owner : this, disposeWhenNodeIsRemoved : element })
	}
)