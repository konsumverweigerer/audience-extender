define([ "knockout", "ext/bootstrap-slider" ], (ko) ->
  sliderDefaults = -> {}

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
)
