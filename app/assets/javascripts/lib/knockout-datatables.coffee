define([ "webjars!knockout.js", "webjars!jquery.dataTables.js", "webjars!jquery.js", "lib/datatables-ext" ], (ko) ->
  defaultoptions = {
    bLengthChange: false,
    sDom: 'lrti',
    fnInfoCallback: (oSettings, iStart, iEnd, iMax, iTotal, sPre) ->
      dl = oSettings._iDisplayLength
      models.datatablescroller.pageSize(dl)
      models.datatablescroller.fromIndex(iStart)
      models.datatablescroller.maxIndex(iMax)
      return ''
  }
  ko.bindingHandlers.datatable = {
    init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      datatableScroller = allBindings.datatableScoller
      datatableOptions = $.extend(defaultoptions, allBindings.datatableOptions || {})
      datatableOptions.fnInfoCallback =  (oSettings, iStart, iEnd, iMax, iTotal, sPre) ->
        dl = oSettings._iDisplayLength
        if datatableScroller
          datatableScroller.pageSize(dl)
          datatableScroller.fromIndex(iStart)
          datatableScroller.maxIndex(iMax)
        return ''

      $datatable = $element.dataTable(datatableOptions)
    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()

      $datatable = $element.dataTable()

      if datatableScroller
        datatableScroller.currentPage.subscribe((nv) ->
          $datatable.fnPageChange(nv - 1)
        )
      val = ko.utils.unwrapObservable(valueAccessor())
      if val == null
        val = []
      else if val.rows
        val.rows()
      $datatable.fnClearTable()
      i = 0
      while i < val.length
        $datatable.fnAddData(val[i++])
  })
