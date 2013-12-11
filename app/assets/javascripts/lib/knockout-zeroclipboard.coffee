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

      $clipboard = new zc($element, zeroclipboardOptions)
      $clipboard.on('wrongflash', (e)->
        $element.addClass 'disabled'
        $element.attr('disabled','true')
        $element.addClass 'zeroclipboard-wrongflash'
      )
      $clipboard.on('noflash', (e)->
        $element.addClass 'disabled'
        $element.attr('disabled','true')
        $element.addClass 'zeroclipboard-noflash'
      )
      $clipboard.off 'datarequested'
      $clipboard.on('datarequested', (e)->
        v = (ko.unwrap valueAccessor())?.replace(/\n/g, '\r\n')
        if v?
          $clipboard.setText v
        $element.addClass 'clipboard-requested'
        if ko.isObservable zeroclipboardOptions.copied
          zeroclipboardOptions.copied v
      )
      $clipboard.off 'complete'
      $clipboard.on('complete', (e)->
        $element.addClass 'clipboard-copied'
        if ko.isObservable zeroclipboardOptions.copied
          zeroclipboardOptions.copied v
        if zeroclipboardOptions.oncomplete?
          if typeof zeroclipboardOptions.oncomplete == 'string'
            $element[zeroclipboardOptions.oncomplete]('show')
          else
            zeroclipboardOptions.oncomplete($element,e)
      )
      v = (ko.unwrap value)?.replace(/\n/g, '\r\n')
      $clipboard.setText v
      $element.data('zeroclipboard',$clipboard)
      $element.data('zeroclipboardOptions',zeroclipboardOptions)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()

      $clipboard = $element.data 'zeroclipboard'
      zeroclipboardOptions = $element.data 'zeroclipboardOptions' || {}
      if $clipboard?
        v = (ko.unwrap value)?.replace(/\n/g, '\r\n')
        $clipboard.setText v
        $clipboard.off 'datarequested'
        $clipboard.on('datarequested', (e)->
          v = (ko.unwrap valueAccessor())?.replace(/\n/g, '\r\n')
          if v?
            $clipboard.setText v
          $element.addClass 'clipboard-requested'
          if ko.isObservable zeroclipboardOptions.copied
            zeroclipboardOptions.copied v
        )
        $clipboard.off 'complete'
        $clipboard.on('complete', (e)->
          $element.addClass 'clipboard-copied'
          if ko.isObservable zeroclipboardOptions.copied
            zeroclipboardOptions.copied v
          if zeroclipboardOptions.oncomplete?
            if typeof zeroclipboardOptions.oncomplete == 'string'
              $element[zeroclipboardOptions.oncomplete]('show')
            else
              zeroclipboardOptions.oncomplete($element,e)
        )
)
