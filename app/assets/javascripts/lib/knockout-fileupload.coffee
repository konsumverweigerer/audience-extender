define([ "knockout", "jquery.fileupload",
 "jquery.iframe-transport", "jquery.fileupload-image", "jquery.fileupload-validate" ], (ko) ->
  fileuploadDefaults = -> {}

  ko.bindingHandlers.fileupload =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      fileuploadOptions = $.extend(fileuploadDefaults(),allBindings.fileuploadOptions || {})

      $fileupload = $element.fileupload()
)
