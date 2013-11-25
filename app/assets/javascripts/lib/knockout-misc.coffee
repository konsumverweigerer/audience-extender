define([ "webjars!knockout.js", "webjars!jquery.js", "ext/bootstrap-slider", "ext/jquery.jcarousel", "ext/wizard" ], (ko) ->
  carouselDefaults = () ->
    {wrap:'both'}
  ko.bindingHandlers.slider = {
    init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
  }
  ko.bindingHandlers.carousel = {
    init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
      allBindings = allBindingsAccessor()
      carouselOptions = $.extend(carouselDefaults(), allBindings.carouselOptions || {})
      $carousel = $element.jcarousel(carouselOptions)
      if carouselOptions.previousButton
        $(carouselOptions.previousButton).on('click',(e)=>
          $element.jcarousel('scroll','-=1')
        )
      if carouselOptions.nextButton
        $(carouselOptions.nextButton).on('click',(e)=>
          $element.jcarousel('scroll','+=1')
        )
    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      $element = $(element)
      val = ko.unwrap(valueAccessor())
      $carousel = $element.jcarousel('scroll',val || 0)
  }
  ko.bindingHandlers.wizard = {
    init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
  }
)