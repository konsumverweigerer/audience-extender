define([ "knockout", "ext/chardinjs" ], (ko) ->
  bootstrapTourDefaults = -> {}

  ko.bindingHandlers.chardin =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      value = valueAccessor()

      val = ko.unwrap value
      $element.off val+'.chardin.ko'
      $element.on(val+'.chardin.ko', (e) ->
        $('body').chardinJs 'start'
      )
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      value = valueAccessor()

      val = ko.unwrap value
      $element.off val+'.chardin.ko'
      $element.on(val+'.chardin.ko', (e) ->
        $('body').chardinJs 'start'
      )
)