define([ "knockout", "ext/bootstrap-tour" ], (ko) ->
  bootstrapTourDefaults = -> {'event':'click'}
  extractTour = (n) ->
    t = []
    i = 1
    for e in $ '[data-tour-name="'+n+'"]'
      $e = $ e
      if not $e.attr 'id'
        $e.attr('id','tour_'+i)
        i = i+1
      t.push [$e.data('tourIndex'),$e.attr('id'),$e.data('tourTitle'),$e.data('tourContent')]
    {'element':'#'+e[1],'title':e[2],'content':e[3]} for e in t.sort()
  ko.bindingHandlers.bootstraptour =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      allBindings = allBindingsAccessor()
      bootstrapTourOptions = $.extend(bootstrapTourDefaults(),allBindings.bootstraptourOptions || {})
      value = valueAccessor()
      val = ko.unwrap value
      ev = bootstrapTourOptions.event
      tour = new Tour({
        steps: extractTour(val)
      })
      tour.init()
      $element.off ev+'.bootstraptour.ko'
      $element.on(ev+'.bootstraptour.ko', (e) ->
        tour.start(true)
      )
      $element.data('tour',tour)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $ element
      allBindings = allBindingsAccessor()
      bootstrapTourOptions = $.extend(bootstrapTourDefaults(),allBindings.bootstrapTourOptions || {})
      value = valueAccessor()
      val = ko.unwrap value
      ev = bootstrapTourOptions.event
      tour = new Tour({
        steps: extractTour(val)
      })
      tour.init()
      $element.off ev+'.bootstraptour.ko'
      $element.on(ev+'.bootstraptour.ko', (e) ->
        tour.start(true)
      )
      $element.data('tour',tour)
)