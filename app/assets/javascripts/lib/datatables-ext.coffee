define([ "webjars!jquery.dataTables.js", "webjars!jquery.js" ], () ->
    $.extend($.fn.dataTableExt.oSort, {
      'currency-asc': (a,b) -> 
        a-b
      'currency-desc': (a,b) -> 
        b-a
      'currency-pre': (a) ->
        if a=='-'
          return 0
        parseFloat(a.replace(/[^\d\-\.]/g,''))
      'percent-asc': (a,b) -> 
        a-b
      'percent-desc': (a,b) -> 
        b-a
      'percent-pre': (a) ->
        if a==''
          return 0
        parseFloat(a.replace(/[^\d\-\.]/g,''))
    })
)