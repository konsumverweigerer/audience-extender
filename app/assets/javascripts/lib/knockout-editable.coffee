define([ "knockout", "bootstrap-editable", "jquery" ], (ko) ->
  $.fn.editable.defaults.mode = 'inline'
  ko.bindingHandlers.editable = {
    init : (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      editableOptions = allBindings.editableOptions || {}

      editableOptions.value = ko.unwrap value

      if !editableOptions.name
        $.each(bindingContext.$data, (k,v) ->
          if v == value
            editableOptions.name = k
            return false
        )

      if !editableOptions.validate && value.isValid
        editableOptions.validate = (testValue) ->
          initalValue = valueAccessor()()
          valueAccessor() testValue
          if valueAccessor().isValid()
            res = null
          else
            res = ko.unwrap valueAccessor().error
          valueAccessor() initalValue
          return res

      if editableOptions.type=='select' || editableOptions.type=='checklist' || editableOptions.type=='typeahead' && !editableOptions.source && editableOptions.options
        if editableOptions.optionsCaption
          editableOptions.prepend = editableOptions.optionsCaption

        applyToObject = (object,predicate,defaultValue) ->
          predicateType = typeof predicate
          if predicateType=='function'
            return predicate object
          else if predicateType=='string'
            return object[predicate]
          else
            return defaultValue

        editableOptions.source = ->
          ko.utils.arrayMap(editableOptions.options(),(item) ->
            {
              value: ko.unwrap applyToObject(item,editableOptions.optionsValue,item)
              text: ko.unwrap applyToObject(item,editableOptions.optionsText,optionText)
            }
          )

      if editableOptions.visible && ko.isObservable(editableOptions.visible)
        editableOptions.toggle = 'manual'

      $editable = $element.editable editableOptions

      if ko.isObservable value
        $editable.on('save.ko', (e,params) ->
          valueAccessor() params.newValue
        )

      if editableOptions.save
        $editable.on('save', editableOptions.save)

      if editableOptions.visible && ko.isObservable editableOptions.visible
        ko.computed
          read: ->
            val = ko.unwrap editableOptions.visible
            if val
              $editable.editable 'show'
          owner: this
          disposeWhenNodeIsRemoved: element
    update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      $element = $ element
      value = valueAccessor()
      allBindings = allBindingsAccessor()

      $editable = $element.editable()

      val = ko.unwrap valueAccessor()
      if val==null
        val = ''
      $editable.editable('setValue',val,true)
  })
