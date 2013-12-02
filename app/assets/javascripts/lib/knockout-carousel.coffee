define([ "knockout", "ext/jquery.jcarousel" ], (ko) ->
  carouselDefaults = -> {wrap:'both'}

  ko.bindingHandlers.carousel =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      allBindings = allBindingsAccessor()
      carouselOptions = $.extend(carouselDefaults(),allBindings.carouselOptions || {})
      $element.jcarousel carouselOptions
      if carouselOptions.previousButton
        $(carouselOptions.previousButton).on('click.ko',(e) =>
          val = ko.unwrap valueAccessor()
          if val.previous
            val.previous()
          else
            $element.jcarousel('scroll','-=1')
        )
      if carouselOptions.nextButton
        $(carouselOptions.nextButton).on('click.ko',(e) =>
          val = ko.unwrap valueAccessor()
          if val.next
            val.next()
          else
            $element.jcarousel('scroll','+=1')
        )
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      val = ko.unwrap valueAccessor()
      fval = val
      if val.currentValue
        val = val.currentValue()
      if val=='' || val=='last'
        allBindings = allBindingsAccessor()
        carouselOptions = $.extend(carouselDefaults(), allBindings.carouselOptions || {})
        $carousel = $element.jcarousel 'destroy'
        $carousel = $element.jcarousel carouselOptions
        ai = $element.jcarousel('items').length
        fi = $element.jcarousel('fullyvisible').length
        if val=='last'
          val = ai-fi
        else
          val = 0
        if fval.currentValue
          fval.maxValue ai-fi
          fval.currentValue val
      else if fval.currentValue
        ai = $element.jcarousel('items').length
        fi = $element.jcarousel('fullyvisible').length
        fval.maxValue ai-fi
      $carousel = $element.jcarousel('scroll',val || 0)
)
