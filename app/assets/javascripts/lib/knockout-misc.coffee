requirejs.config
  map:
    '/webjars/jquery-file-upload/8.4.2/js':
      '/webjars/jquery-file-upload/8.4.2/js/./jquery.fileupload': 'webjars!jquery.fileupload.js'
      '/webjars/jquery-file-upload/8.4.2/js/./jquery.fileupload-process': 'webjars!jquery.fileupload-process.js'
  shim:
    'webjars!jquery.ui.widget.js':
      deps: [ 'webjars!jquery.js' ]
    'webjars!jqBootstrapValidation.js':
      deps: [ 'webjars!jquery.js' ]

define("load-image", [ "ext/load-image" ], -> )
define("load-image-meta", [ ], -> )
define("load-image-exif", [ ], -> )
define("load-image-ios", [ ], -> )
define("canvas-to-blob", [ "ext/canvas-to-blob" ], -> )
define("jquery.ui.widget", [ "webjars!jquery.ui.widget.js" ], -> )

define([ "webjars!knockout.js", "webjars!jquery.fileupload.js",
 "webjars!jquery.iframe-transport.js", "webjars!jquery.fileupload-image.js", "webjars!jquery.fileupload-validate.js",
 "webjars!jqBootstrapValidation.js", "ext/bootstrap-slider", "ext/jquery.jcarousel", "ext/wizard" ], (ko) ->
  carouselDefaults = -> {wrap:'both'}
  wizardDefaults = -> {}
  sliderDefaults = -> {}
  fileuploadDefaults = -> {}

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
        else
          if newValue!=current
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
        else
          if newValue!=current
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
        else
          if newValue!=current
            target.notifySubscribers valueToWrite
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
          element.checked = ko.utils.arrayIndexOf(modelValue,checkedValue())>=0
        else if isCheckbox
          element.checked = modelValue
        else if isRadio
          if checkedValue()==modelValue
            $element.parent('label').button('toggle')
        else
          element.checked = (checkedValue()==modelValue)

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

  ko.bindingHandlers.carousel =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      allBindings = allBindingsAccessor()
      carouselOptions = $.extend(carouselDefaults(),allBindings.carouselOptions || {})
      $element.jcarousel carouselOptions
      if carouselOptions.previousButton
        $(carouselOptions.previousButton).on('click.ko',(e) =>
          val = ko.unwrap valueAccessor()
          if val.previous
            val.previous()
          else
            $element.jcarousel('scroll','-=1')
        )
      if carouselOptions.nextButton
        $(carouselOptions.nextButton).on('click.ko',(e) =>
          val = ko.unwrap valueAccessor()
          if val.next
            val.next()
          else
            $element.jcarousel('scroll','+=1')
        )
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      val = ko.unwrap valueAccessor()
      fval = val
      if val.currentValue
        val = val.currentValue()
      if val=='' || val=='last'
        allBindings = allBindingsAccessor()
        carouselOptions = $.extend(carouselDefaults(), allBindings.carouselOptions || {})
        $carousel = $element.jcarousel 'destroy'
        $carousel = $element.jcarousel carouselOptions
        ai = $element.jcarousel('items').length
        fi = $element.jcarousel('fullyvisible').length
        if val=='last'
          val = ai-fi
        else
          val = 0
        if fval.currentValue
          fval.maxValue ai-fi
          fval.currentValue val
      else if fval.currentValue
        ai = $element.jcarousel('items').length
        fi = $element.jcarousel('fullyvisible').length
        fval.maxValue ai-fi
      $carousel = $element.jcarousel('scroll',val || 0)

  ko.bindingHandlers.wizard =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
      wizardOptions = $.extend(wizardDefaults(), allBindings.wizardOptions || {})
      $element.wizard wizardOptions
      cs = $element.wizard 'selectedItem'
      if cs
        css = cs.step
        while css!=val
          if css<val
            $element.wizard 'next'
          else if css>val
            $element.wizard 'previous'
          cs = $element.wizard 'selectedItem'
          if css==cs.step
            break
          css = cs.step
      $element.on('changed.ko',(e) =>
        cs = $element.wizard 'selectedItem'
        if cs
          valueAccessor() cs.step
      )
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
      $element.off 'changed.ko'
      cs = $element.wizard 'selectedItem'
      if cs
        css = cs.step
        while css!=val
          if css<val
            $element.wizard 'next'
          else if css>val
            $element.wizard 'previous'
          cs = $element.wizard 'selectedItem'
          if css==cs.step
            break
          css = cs.step
      $element.on('changed.ko',(e) =>
        cs = $element.wizard 'selectedItem'
        if cs
          valueAccessor() cs.step
      )

  ko.bindingHandlers.fadeVisible =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      $element.toggle ko.unwrap value
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      value = valueAccessor()
      if ko.unwrap value
        $(element).fadeIn(200)
      else
        $(element).fadeOut(200)

  ko.bindingHandlers.slider =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      sliderOptions = $.extend(sliderDefaults(),allBindings.sliderOptions || {})

      $slider = $element.slider sliderOptions
      if ko.isObservable value
        $slider.on('slide.ko', (e) ->
          valueAccessor() e.value
        )

      if sliderOptions.slide
        $slider.on('slide',sliderOptions.slide)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()

      $slider = $element.slider()
      val = ko.utils.unwrapObservable value
      if not val?
        val = 0
      $slider.slider('setValue',val)

  ko.bindingHandlers.fileupload =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      fileuploadOptions = $.extend(fileuploadDefaults(),allBindings.fileuploadOptions || {})

      $fileupload = $element.fileupload()

  ko.bindingHandlers.jqvalidation =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
)
