define([ "knockout", "ext/jquery.noty" ], (ko) ->
  notyDefaults = -> {
      theme: 'bootstrap'
      layout: 'topLeft'
      type: 'information'
      closeWith: ['button']
      template:'<div class="noty_message"><div class="noty_close"></div><span class="noty_text"></span></div>',
      animation:
        open:
          opacity: 'toggle'
        close:
          opacity: 'toggle'
        easing: 'swing'
        speed: 100
    }

  sendnoty = (val,opts) ->
    if val?.indexOf?
      sendnoty(v,opts) for v in val
    else if val?
      if val.type? && val.text?
        if !val.shown?
          o = $.extend(opts,{text: val.text,type: val.type})
          val.shown = true
      else
        o = $.extend(opts,{text: val})
      if o?
        if opts?.container? && opts?.localMsg?
          c = opts.container
          if c instanceof String
            c = $(c)
          c.noty o
        else
          $.notyRenderer.init o

  ko.bindingHandlers.noty =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
      notyOptions = $.extend(notyDefaults(), allBindings.notyOptions || {})
      notyOptions.container = $element
      sendnoty(val,notyOptions)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      val = ko.unwrap valueAccessor()
      allBindings = allBindingsAccessor()
      notyOptions = $.extend(notyDefaults(), allBindings.notyOptions || {})
      notyOptions.container = $element
      sendnoty(val,notyOptions)
)
