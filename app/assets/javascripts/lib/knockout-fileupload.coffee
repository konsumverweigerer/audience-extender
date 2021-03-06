define([ "knockout", "jquery.fileupload", "jquery",
 "jquery.iframe-transport", "jquery.fileupload-image", "jquery.fileupload-validate" ], (ko) ->
  fileuploadDefaults = -> {
      dataType: 'json'
      autoUpload: true
      acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i
      maxFileSize: 5000000
      disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator && navigator.userAgent)
      previewMaxWidth: 100
      previewMaxHeight: 100
      previewCrop: true
    }

  attachfileupload = ($fileupload,fileuploadOptions,valueAccessor) ->
    $fileupload.off '.fileupload.ko'

    if fileuploadOptions.progress?
      $fileupload.on('fileuploadprogressall.fileupload.ko', (e,data) ->
        progress = parseInt(data.loaded/data.total*100,10)
        if ko.isObservable fileuploadOptions.progress
          fileuploadOptions.progress progress
        else
          $(fileuploadOptions.progress).css('width',progress + '%')
      )
    else if fileuploadOptions.fileuploadprogressall?
      $fileupload.on('fileuploadprogressall.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploadprogressall(e,data)
      )
    if fileuploadOptions.fileuploadadd?
      $fileupload.on('fileuploadadd.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploadadd(e,data)
      )
    if fileuploadOptions.fileuploadprocessalways?
      $fileupload.on('fileuploadprocessalways.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploadprocessalways(e,data)
      )
    if fileuploadOptions.fileuploadprocessfail?
      $fileupload.on('fileuploadprocessfail.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploadprocessfail(e,data)
      )
    if fileuploadOptions.fileuploadprocessdone?
      $fileupload.on('fileuploadprocessdone.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploadprocessdone(e,data)
      )
    if fileuploadOptions.fileuploaddone?
      $fileupload.on('fileuploaddone.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploaddone(e,data.result)
      )
    if valueAccessor()?
      $fileupload.on('fileuploaddone.fileupload.ko.va', (e,data) ->
        valueAccessor() data.result
      )
    if fileuploadOptions.fileuploadfail?
      $fileupload.on('fileuploadfail.fileupload.ko', (e,data) ->
        fileuploadOptions.fileuploadfail(e,data)
      )
    if ko.isObservable fileuploadOptions.errors
      $fileupload.on('fileuploadfail.fileupload.ko.va', (e,data) ->
        fileuploadOptions.errors (f.error for f in data.result.files)
      )

  preprocess = (fileuploadOptions) ->
    if fileuploadOptions.route?
      fileuploadOptions.url = fileuploadOptions.route.url
    if fileuploadOptions.dropZone? && (fileuploadOptions.dropZone instanceof String || typeof(fileuploadOptions.dropZone)=='string')
      fileuploadOptions.dropZone = $(fileuploadOptions.dropZone)
    if fileuploadOptions.pasteZone? && (fileuploadOptions.pasteZone instanceof String || typeof(fileuploadOptions.pasteZone)=='string')
      fileuploadOptions.pasteZone = $(fileuploadOptions.pasteZone)

  ko.bindingHandlers.fileupload =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      allBindings = allBindingsAccessor()
      fileuploadOptions = $.extend(fileuploadDefaults(),allBindings.fileuploadOptions || {})
      preprocess fileuploadOptions
      $fileupload = $element.fileupload fileuploadOptions
      attachfileupload($fileupload,fileuploadOptions,valueAccessor)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      allBindings = allBindingsAccessor()
      fileuploadOptions = $.extend(fileuploadDefaults(),allBindings.fileuploadOptions || {})
      preprocess fileuploadOptions
      $fileupload = $element.fileupload fileuploadOptions
      attachfileupload($fileupload,fileuploadOptions,valueAccessor)
)
