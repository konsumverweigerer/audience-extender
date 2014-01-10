define([ "knockout", "bootstrap-datepicker", "jquery" ], (ko) ->
  addperiod = (date,period,num,fallback) ->
    d = new Date()
    if period=='days'
      d.setDate(date.getDate()+num)
    else if period=='weeks'
      d.setDate(date.getDate()+7*num)
    else if period=='months'
      d.setMonth(date.getMonth()+num)
    else
      return fallback
    d
  onlyFuture = (d) ->
    t = (new Date()).getTime()
    d.getTime()>=t

  ko.bindingHandlers.datepicker =
    init: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      datepickerOptions = allBindings.datepickerOptions || {}
      
      if $element.is('.input-daterange')
        inps = $element.find('input.date-picker').next().parent()
        if inps.length
          datepickerOptions.inputs = inps
      if datepickerOptions.onlyFuture?
        datepickerOptions.beforeShowDay = onlyFuture
      $datepicker = $element.datepicker datepickerOptions

      fixzindex = (e) -> $($(e).data('datepicker').picker).css('z-index',10000)

      if $element.is('.input-daterange')
        dp = $datepicker.data('datepicker').pickers
        if ko.isObservable(value) || ko.isObservable(value.dates)
          $(dp[0].element).on('changeDate.ko', (e) ->
            v = valueAccessor()
            if v?.dates
              v = v.dates
            cv = v() ? [ new Date(), new Date() ]
            period = datepickerOptions.fixedPeriod && ko.unwrap datepickerOptions.fixedPeriod
            v([e.date,addperiod(e.date,period,1,cv[1])])
          )
          $(dp[1].element).on('changeDate.ko', (e) ->
            v = valueAccessor()
            if v?.dates
              v = v.dates
            cv = v() ? [ new Date(), new Date() ]
            period = datepickerOptions.fixedPeriod && ko.unwrap datepickerOptions.fixedPeriod
            v([addperiod(e.date,period,-1,cv[0]),e.date])
          )
        $(dp[0].element).on('show.ko', (e) -> fixzindex dp[0].element )
        $(dp[1].element).on('show.ko', (e) -> fixzindex dp[1].element )
      else 
        if ko.isObservable value
          $datepicker.on('changeDate.ko', (e) -> valueAccessor() e.date )
        $datepicker.on('show.ko', (e) -> fixzindex $datepicker )

      if datepickerOptions.changeData
        $datepicker.on('changeDate',datepickerOptions.changeDate)
    update: (element,valueAccessor,allBindingsAccessor,viewModel,bindingContext) ->
      $element = $(element)
      value = valueAccessor()

      $datepicker = $element.datepicker()

      if $datepicker.is '.input-daterange'
        val = ko.unwrap value
        if val?.dates
          val = val.dates()
        if not val?
          val = []
        dr = $datepicker.data 'datepicker'
        dp = dr.pickers
        if val.length==2 && dp.length==2
          dp[0].setDate(val[0] ? new Date())
          dp[1].setDate(val[1] ? new Date())
        dr.updateDates()
      else
        val = ko.utils.unwrapObservable value
        if not val?
          val = ''
        $datepicker.datepicker('setDate',val)
)
