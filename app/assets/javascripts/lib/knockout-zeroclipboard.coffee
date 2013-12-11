define([ "knockout", "ext/ZeroClipboard", "jquery" ], (ko, zc) ->
  zeroclipboardDefaults = -> {
    moviePath: '/assets/images/ZeroClipboard.swf'
  }

  ko.bindingHandlers.zeroclipboard =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      zeroclipboardOptions = $.extend(zeroclipboardDefaults(),allBindings.zeroclipboardOptions || {})

      $clipboard = new zc(undefined, zeroclipboardOptions)
      $clipboard.unglue $element
      $clipboard.glue $element
      v = (ko.unwrap value)?.replace(/\n/g, '\r\n')
      $clipboard.setText v
      $element.data('clipboardText',v)
      $element.data('zeroclipboard',$clipboard)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      zeroclipboardOptions = $.extend(zeroclipboardDefaults(),allBindings.zeroclipboardOptions || {})

      $clipboard = $element.data 'zeroclipboard'
      if $clipboard?
        v = (ko.unwrap value)?.replace(/\n/g, '\r\n')
        $clipboard.setText v
        $element.data('clipboardText',v)
)
