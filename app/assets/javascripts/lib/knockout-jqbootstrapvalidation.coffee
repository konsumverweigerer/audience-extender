define([ "knockout", "jqBootstrapValidation" ], (ko) ->
  ko.bindingHandlers.jqvalidation =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
)
