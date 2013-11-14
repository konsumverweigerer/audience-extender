define([ "webjars!knockout.js", "webjars!bootstrap-editable.js", "webjars!jquery.js" ], (ko) ->
	ko.bindingHandlers.editable = {
		init : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
			$element = $(element)
			value = valueAccessor()
			allBindings = allBindingsAccessor()
			editableOptions = allBindings.editableOptions || {}

			editableOptions.value = ko.utils.unwrapObservable(value)

			if !editableOptions.name
				$.each(bindingContext.$data, (k, v) ->
					if v == value
						editableOptions.name = k
						return false
				)

			if !editableOptions.validate && value.isValid
				editableOptions.validate = (testValue) ->
					initalValue = value()
					value(testValue)
					res = value.isValid() ? null : ko.utils.unwrapObservable(value.error)
					value(initalValue)
					return res;

			if editableOptions.type == 'select' || editableOptions.type == 'checklist' || editableOptions.type == 'typeahead' && !editableOptions.source && editableOptions.options
				if editableOptions.optionsCaption
					editableOptions.prepend = editableOptions.optionsCaption

				applyToObject = (object, predicate, defaultValue) ->
					predicateType = typeof predicate
					if predicateType == "function"
						return predicate(object)
					else if predicateType == "string"
						return object[predicate]
					else
						return defaultValue

				editableOptions.source = () ->
					ko.utils.arrayMap(editableOptions.options(),(item) ->
						optionValue = applyToObject(item,editableOptions.optionsValue,item)
						optionText = applyToObject(item,editableOptions.optionsText,optionText)
						{ value : ko.utils.unwrapObservable(optionValue),text : ko.utils.unwrapObservable(optionText)}
					)

			if editableOptions.visible && ko.isObservable(editableOptions.visible)
				editableOptions.toggle = 'manual'

			$editable = $element.editable(editableOptions)

			if ko.isObservable(value)
				$editable.on('save.ko', (e, params) ->
					value(params.newValue)
				)

			if editableOptions.save
				$editable.on('save', editableOptions.save)

			ko.computed({ read : () ->
				val = ko.utils.unwrapObservable(valueAccessor())
				if val == null
					val = '';
				$editable.editable('setValue', val, true)
			, owner : this, disposeWhenNodeIsRemoved : element })

			if editableOptions.visible && ko.isObservable(editableOptions.visible)
				ko.computed({ read : () ->
					val = ko.utils.unwrapObservable(editableOptions.visible())
					if (val)
						$editable.editable('show')
				, owner : this, disposeWhenNodeIsRemoved : element })

				$editable.on('hidden.ko', (e, params) ->
					editableOptions.visible(false)
				)
	}
)