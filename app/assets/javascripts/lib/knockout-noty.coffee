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
      if val.title? && val.content? && val.priority?
        v = 
          type: ko.unwrap val.priority
        if val.dismissed? && ko.unwrap val.dismissed
          v.shown = true
        else if val.dismiss?
          val.dismiss()
        t = ko.unwrap val.title
        if t? && t!=''
          v.text = '<h4>'+t+'</h4>'+(ko.unwrap(val.content))
        else
          v.text = ko.unwrap val.content
        val = v
      if val.type? && val.text?
        if !val.shown?
          o = $.extend({},opts,{text: val.text,type: val.type})
          val.shown = true
      else
        o = $.extend({},opts,{text: val})
      if o?
        if o.type=='error'
          o.killer = true
          o.force = true
        else
          o.timeout = 10000
        if opts?.container? && opts?.localMsg?
          c = opts.container
          if c instanceof String || typeof(c)=='string'
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
