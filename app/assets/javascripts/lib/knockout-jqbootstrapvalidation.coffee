define([ "knockout", "jqBootstrapValidation" ], (ko) ->
  validationDefaults = -> { targets: ':input' }

  ko.bindingHandlers.jqvalidation =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      jqvalidationOptions = $.extend(validationDefaults(),ko.unwrap(value) || {})

      if jqvalidationOptions.targets?
        $targets = $element.find(jqvalidationOptions.targets)
      else
        $targets = $element
      $targets.jqBootstrapValidation(jqvalidationOptions)
      
      if jqvalidationOptions.messages? && ko.observable jqvalidationOptions.messages
        $targets.parents('form').on('submit.ko', ->
          v = $targets.map((el) -> $(el).triggerHandler('validation.validation'))
          jqvalidationOptions.messages(v.reduce(((a,b) -> a.concat(b)),[]))
        )
)
