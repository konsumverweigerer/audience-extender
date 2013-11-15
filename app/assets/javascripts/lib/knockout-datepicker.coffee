define([ "webjars!knockout.js", "webjars!bootstrap-datepicker.js", "webjars!jquery.js" ], (ko) ->
	ko.bindingHandlers.datepicker = {
		init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()
			datepickerOptions = allBindings.datepickerOptions || {}

			if !datepickerOptions.name
				$.each(bindingContext.$data, (k, v) ->
					if v == value
						datepickerOptions.name = k
						return false
				)

			if !datepickerOptions.validate && value.isValid
				datepickerOptions.validate = (testValue) ->
					initalValue = valueAccessor()()
					valueAccessor()(testValue)
					res = valueAccessor().isValid() ? null : ko.utils.unwrapObservable(valueAccessor().error)
					valueAccessor()(initalValue)
					return res

			$datepicker = $element.datepicker(datepickerOptions)

			if ko.isObservable(value)
				if $datepicker.is('.input-daterange')
					$dp = $datepicker.data("datepicker").pickers
					$dp[0].on('save.ko', (e, params) ->
						v = valueAccessor()()
						valueAccessor()([params.newValue,v[1]])
					)					
					$dp[1].on('save.ko', (e, params) ->
						v = valueAccessor()()
						valueAccessor()([v[0],params.newValue])
					)					
				else
					$datepicker.on('save.ko', (e, params) ->
						valueAccessor()(params.newValue)
					)

			if datepickerOptions.save
				$datepicker.on('save', datepickerOptions.save)

		, update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()
			
			$datepicker = $element.datepicker()
			
			if $datepicker.is('.input-daterange')
				val = ko.utils.unwrapObservable(valueAccessor())
				if val == null
					val = [];
				$dp = $datepicker.data("datepicker").pickers
				if val.length == 2 && $dp.length == 2
					$dp[0].dadatepicker('setDate', val[0])
					$dp[1].dadatepicker('setDate', val[1])
			else
				val = ko.utils.unwrapObservable(valueAccessor())
				if val == null
					val = '';
				$datepicker.datepicker('setDate', val)
	}
)