define([ "knockout", "ext/wizard" ], (ko) ->
  wizardDefaults = -> {}

  ko.bindingHandlers.wizard =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
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
      $element = $ element
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
)
