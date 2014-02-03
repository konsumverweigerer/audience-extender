define([ "knockout", "ext/jquery.jcarousel" ], (ko) ->
  carouselDefaults = -> {wrap:'both'}

  ko.bindingHandlers.carousel =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      allBindings = allBindingsAccessor()
      carouselOptions = $.extend(carouselDefaults(),allBindings.carouselOptions || {})
      c = $element.jcarousel carouselOptions
      if carouselOptions.pagination?
        o = $.extend({},carouselOptions.paginationOptions,{carousel: c})
        if carouselOptions.paginationOptions?.itemType?
          switch carouselOptions.paginationOptions.itemType
            when 'bullet'
              o.item = (page) ->
                '<a href="#' + page + '">*</a>'
        $(carouselOptions.pagination).jcarouselPagination o
      if carouselOptions.previousButton?
        $(carouselOptions.previousButton).off 'click.ko-carousel'
        $(carouselOptions.previousButton).on('click.ko-carousel',(e) =>
          val = ko.unwrap valueAccessor()
          if val.previous
            val.previous()
          else
            $element.jcarousel('scroll','-=1')
        )
      if carouselOptions.nextButton?
        $(carouselOptions.nextButton).off 'click.ko-carousel'
        $(carouselOptions.nextButton).on('click.ko-carousel',(e) =>
          val = ko.unwrap valueAccessor()
          if val.next
            val.next()
          else
            $element.jcarousel('scroll','+=1')
        )
      $element.off 'jcarousel:animateend.ko-carousel'
      $element.on('jcarousel:animateend.ko-carousel',(e,v) =>
        p = v.items().index v.closest()
        val = ko.unwrap valueAccessor()
        if val.currentValue?
          val.currentValue p
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
        c = $element.jcarousel carouselOptions
        if carouselOptions.pagination?
          o = $.extend({},carouselOptions.paginationOptions,{carousel: c})
          if carouselOptions.paginationOptions?.itemType?
            switch carouselOptions.paginationOptions.itemType
              when 'bullet'
                o.item = (page) ->
                  '<a href="#' + page + '">*</a>'
          $(carouselOptions.pagination).jcarouselPagination o
        if carouselOptions.previousButton?
          $(carouselOptions.previousButton).off('click.ko-carousel')
          $(carouselOptions.previousButton).on('click.ko-carousel',(e) =>
            val = ko.unwrap valueAccessor()
            if val.previous
              val.previous()
            else
              $element.jcarousel('scroll','-=1')
          )
        if carouselOptions.nextButton?
          $(carouselOptions.nextButton).off('click.ko-carousel')
          $(carouselOptions.nextButton).on('click.ko-carousel',(e) =>
            val = ko.unwrap valueAccessor()
            if val.next
              val.next()
            else
              $element.jcarousel('scroll','+=1')
          )
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
      c = $element.jcarousel('scroll',val || 0)
      $element.off 'jcarousel:animateend.ko-carousel'
      $element.on('jcarousel:animateend.ko-carousel',(e,v) =>
        p = v.items().index v.closest()
        val = ko.unwrap valueAccessor()
        if val.currentValue?
          val.currentValue p
      )
)
