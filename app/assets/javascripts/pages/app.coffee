define(["jquery", "bootstrap"], ->
  scrollPos = ->
    $('html, body').map((i,a) -> $(a).scrollTop()).toArray().reduce((x,y) -> Math.max(x,y))

  $(document).ready =>
    firstoffs = 100
    speed = 500
    $(".scroll").on('click.ae', (event) ->
      event.preventDefault()
      dest = 0
      winheight = $(window).height()
      docheight = $(document).height()
      offs = $('a[name='+@hash.substr(1)+']').offset().top
      maxdest = docheight-winheight
      if offs > maxdest && offs > firstoffs
        dest = maxdest - firstoffs
      else if offs > firstoffs
        dest = offs - firstoffs
      $('html,body').animate({scrollTop:dest}, speed,'swing')
    )
    $('.scroll-prev').on('click.ae', (event) =>
      current = scrollPos()
      pos = 0
      $('a.scroll').each (i,el) =>
        offs = $('a[name='+el.hash.substr(1)+']').offset().top
        if offs < current && offs > pos
          pos = offs - firstoffs
      if pos < 0
        pos = 0
      $('html,body').animate({scrollTop:pos}, speed,'swing')
    )
    $('.scroll-next').on('click.ae', (event) =>
      current = scrollPos()
      winheight = $(window).height()
      docheight = $(document).height()
      maxpos = docheight-winheight
      pos = maxpos - firstoffs
      $('a.scroll').each (i,el) =>
        offs = $('a[name='+el.hash.substr(1)+']').offset().top
        if offs > (current + firstoffs + 10) && offs < pos
          pos = offs - firstoffs
      if pos < 0
        pos = 0
      $('html,body').animate({scrollTop:pos},speed,'swing')
    )
    $(window).on('scroll.ae', (event) =>
      pos = scrollPos()
      winheight = $(window).height()
      docheight = $(document).height()
      maxpos = docheight-winheight
      if pos>200
        $('.scroll-prev').fadeIn()
      else
        $('.scroll-prev').fadeOut()
      if pos>(maxpos-200)
        $('.scroll-next').fadeOut()
      else
        $('.scroll-next').fadeIn()
    )
    $('.scroll-next,.scroll-prev').hide().css('visibility','visible')
    $('body').on('click.ae','.navbar-collapse a', ->
      el = $(@)
      setTimeout( ->
        el.closest('.navbar-collapse').collapse 'hide'
      ,300)
    )
)
