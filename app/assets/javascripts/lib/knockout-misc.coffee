define([ "knockout", "momentjs" ], (ko) ->
  ko.extenders.datetime = (target,mode) ->
    result = ko.computed(
      read: ->
        v = target()
        if v instanceof Date
          return v
        return undefined
      write: (newValue) ->
        current = target()
        require(['moment'], (moment) ->
          if newValue instanceof Date
            valueToWrite = newValue
          else if (newValue instanceof String || typeof(newValue)=='string') && newValue.length>0
            valueToWrite = moment(newValue)
            if !valueToWrite.isValid()
              valueToWrite = moment(newValue,'MM/DD/YYYY')
            if !valueToWrite.isValid()
              valueToWrite = moment(newValue,'MM/DD/YY')
            if !valueToWrite.isValid()
              valueToWrite = moment(newValue,'DD.MM.YYYY')
            if !valueToWrite.isValid()
              valueToWrite = moment(newValue,'DD.MM.YY')
            if !valueToWrite.isValid()
              valueToWrite = undefined
          if valueToWrite!=current
            target valueToWrite
          else if newValue!=current
            target.notifySubscribers valueToWrite
        )
    ).extend { notify: 'always' }
 
    result target()
    return result

  ko.extenders.numeric = (target,precision) ->
    result = ko.computed(
      read: target
      write: (newValue) ->
        current = target()
        roundingMultiplier = Math.pow(10,precision)
        newValueAsNum = parseFloat +newValue
        if isNaN newValueAsNum
          newValueAsNum = 0
        valueToWrite = Math.round(newValueAsNum*roundingMultiplier)/roundingMultiplier
        if valueToWrite!=current
          target valueToWrite
        else if newValue!=current
          target.notifySubscribers valueToWrite
    ).extend { notify: 'always' }
 
    result target()
    return result

  ko.extenders.integers = (target,base) ->
    result = ko.computed(
      read: target
      write: (newValue) ->
        current = target()
        newValueAsNum = parseFloat +newValue
        if isNaN newValueAsNum
          newValueAsNum = 0
        valueToWrite = Math.abs Math.round newValueAsNum
        if valueToWrite!=current
          target valueToWrite
        else if newValue!=current
            target.notifySubscribers valueToWrite
    ).extend { notify: 'always' }
 
    result target()
    return result

  ko.extenders.percent = (target,precision) ->
    result = ko.computed(
      read: ->
        target()+'%'
      write: (newValue) ->
        current = target()
        roundingMultiplier = Math.pow(10,precision)
        v = (''+newValue)
        p = (newValue instanceof String || typeof(newValue)=='string') && v.indexOf('%')<0
        newValueAsNum = parseFloat +(v.replace(/[%]/,''))
        if isNaN newValueAsNum
          newValueAsNum = 0
        if p
          newValueAsNum = newValueAsNum*100
        valueToWrite = Math.round(newValueAsNum*roundingMultiplier)/roundingMultiplier
        if valueToWrite!=current
          target valueToWrite
        else if newValue!=current
            target.notifySubscribers valueToWrite
    ).extend { notify: 'always' }
 
    result target()
    return result

  ko.extenders.currency = (target,lang) ->
    result = ko.computed(
      read: target
      write: (newValue) ->
        current = target()
        roundingMultiplier = 1
        symbolPre = ''
        symbolPost = ''
        if lang=='us'
          roundingMultiplier = 100
          symbolPre = '$'
        newValueAsNum = parseFloat +((''+newValue).replace(/[^0-9,.-]/,''))
        if isNaN newValueAsNum
          newValueAsNum = 0
        valueToWrite = symbolPre+(Math.round(newValueAsNum*roundingMultiplier)/roundingMultiplier)+symbolPost
        if valueToWrite!=current
          target valueToWrite
        else if newValue!=current
            target.notifySubscribers valueToWrite
    ).extend { notify: 'always' }
 
    result target()
    return result

  ko.extenders.tail = (target,num) ->
    result = ko.computed(
      read: ->
        val = target()
        if val?.indexOf?
          t = val.length
          f = t-num
          if f<=0
            return val
          return (v for v in val[f...t])
        return val
      write: (newValue) ->
    ).extend { notify: 'always' }
 
    result target()
    return result

  ko.bindingHandlers.checkedChange =
    after: ['value','attr']
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      allBindings = allBindingsAccessor()
      checkedValue = ->
        if allBindings.checkedValue
          ko.utils.unwrap allBindings.checkedValue
        else
          element.value

      updateModel = ->
        isChecked = element.checked
        elemValue = isChecked
        if useCheckedValue
          elemValue = checkedValue()
        if !shouldSet
          return
        if isRadio && !isChecked
          return

        if isValueArray
          modelValue = ko.unwrap valueAccessor()
          if oldElemValue!=elemValue
            if isChecked
              ko.utils.addOrRemoveItem(modelValue,elemValue,true)
              ko.utils.addOrRemoveItem(modelValue,oldElemValue,false)
            oldElemValue = elemValue
          else
            ko.utils.addOrRemoveItem(modelValue,elemValue,isChecked)
        else
          valueAccessor() elemValue

      updateView = ->
        $element = $(element)
        modelValue = ko.unwrap valueAccessor()
        if isValueArray
          $element.prop('checked',ko.utils.arrayIndexOf(modelValue,checkedValue())>=0)
        else if isCheckbox
          $element.prop('checked',modelValue)
        else if isRadio
          if checkedValue()==modelValue
            if !$element.prop('checked')
              $element.parent('label').button('toggle')
          else
            if $element.prop('checked')
              $element.parent('label').button('toggle')
        else
          $element.prop('checked',checkedValue()==modelValue)

      isCheckbox = element.type == "checkbox"
      isRadio = element.type == "radio"

      if !isCheckbox && !isRadio
        return

      $element = $(element)
      isValueArray = isCheckbox && (ko.utils.unwrapObservable valueAccessor() instanceof Array)
      oldElemValue = undefined
      if isValueArray
        oldElemValue = checkedValue()
      useCheckedValue = isRadio || isValueArray
      shouldSet = false

      if isRadio && !element.name
        ko.bindingHandlers['uniqueName']['init'](element, -> true)

      ko.dependentObservable(updateModel, null, { disposeWhenNodeIsRemoved: element })
      $element.on("click.ko", updateModel)
      $element.on("change.ko", updateModel)
      ko.dependentObservable(updateView, null, { disposeWhenNodeIsRemoved: element })
      shouldSet = true

  ko.bindingHandlers.fadeVisible =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      $element.toggle ko.unwrap value
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      value = valueAccessor()
      if ko.unwrap value
        setTimeout( -> 
          $(element).fadeIn(400)
        ,105)
      else
        $(element).fadeOut(100)
)
