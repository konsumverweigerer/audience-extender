define([ "webjars!knockout.js", "webjars!jquery.js", "ext/bootstrap-slider", "ext/jquery.carousel", "ext/wizard" ], (ko) ->
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
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      val = ko.unwrap(valueAccessor())
      allBindings = allBindingsAccessor()
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