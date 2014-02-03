define([ "knockout", "jquery.dataTables", "jquery", "lib/datatables-ext" ], (ko) ->
  defaultOptions = -> {
    bLengthChange: false
    sDom: 'lrti'
    fnInfoCallback: (oSettings, iStart, iEnd, iMax, iTotal, sPre) ->
      dl = oSettings._iDisplayLength
      models.datatablescroller.pageSize(dl)
      models.datatablescroller.fromIndex(iStart)
      models.datatablescroller.maxIndex(iMax)
      return ''
    }
  ko.bindingHandlers.datatable =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      datatableScroller = allBindings.datatableScroller || value.scroller

      datatableOptions = $.extend(defaultOptions(),allBindings.datatableOptions || {})
      datatableOptions.fnInfoCallback = (oSettings,iStart,iEnd,iMax,iTotal,sPre) ->
        dl = oSettings._iDisplayLength
        if datatableScroller && not datatableScroller.updating()
          datatableScroller.params(iStart,iMax,dl)
        return ''

      if datatableScroller
        datatableScroller.currentPage.subscribe (nv) ->
          $datatable.fnPageChange nv-1

      $datatable = $element.dataTable datatableOptions
      if value.rows && ko.isObservable value.rows
        value.rows.extend
          throttle:100
        .subscribe (nv) ->
          ko.bindingHandlers.datatable.update(element,value.rows,allBindingsAccessor,viewModel,bindingContext)
        if datatableOptions.rowClick || value.rowClick
          $datatable.on('click.ko-datatables','tbody tr',(e) ->
            r = $datatable.fnGetData @
            if r?
              (datatableOptions.rowClick || value.rowClick) value.data()[r.row]
          )
          $datatable.on('mouseenter.ko-datatables','tbody tr',(e) ->
            $(e.target).css('cursor','pointer')
          )
      else if datatableOptions.rowClick || value.rowClick
        $datatable.on('click.ko-datatables','tbody tr',(e) ->
          (datatableOptions.rowClick || value.rowClick) $datatable.fnGetData @
        )
        $datatable.on('mouseenter.ko-datatables','tbody tr',(e) ->
          $(e.target).css('cursor','pointer')
        )
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      allBindings = allBindingsAccessor()
      datatableScroller = allBindings.datatableScroller || value.scroller

      $datatable = $element.dataTable()

      val = ko.utils.unwrapObservable valueAccessor()
      if val == null
        val = []
      else if val.rows
        val = val.rows()
      if datatableScroller
        datatableScroller.updating true
      $datatable.fnClearTable()
      dat = ($.extend({row:i},d) for d,i in val)
      $datatable.fnAddData dat
      if datatableScroller
        datatableScroller.updating false
        datatableScroller.params(1,dat.length)
)
