define([ "webjars!knockout.js", "webjars!bootstrap-datepicker.js", "webjars!jquery.js" ], (ko) ->
  ko.bindingHandlers.datepicker = {
    init : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      datepickerOptions = allBindings.datepickerOptions || {}

      $datepicker = $element.datepicker(datepickerOptions)

      if $datepicker.is('.input-daterange') && ko.isObservable(value.dates)
        dp = $datepicker.data('datepicker').pickers
        $(dp[0].element).on('changeDate.ko', (e) ->
          v = valueAccessor().dates()
          valueAccessor().dates([e.date,v[1]])
        )
        $(dp[1].element).on('changeDate.ko', (e) ->
          v = valueAccessor().dates()
          valueAccessor().dates([v[0],e.date])
        )
      else if ko.isObservable(value)
        $datepicker.on('changeDate.ko', (e) ->
          valueAccessor()(e.date)
        )

      if datepickerOptions.changeData
        $datepicker.on('changeDate', datepickerOptions.changeDate)

    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()

      $datepicker = $element.datepicker()

      if $datepicker.is('.input-daterange')
        val = ko.utils.unwrapObservable(value).dates()
        if val == null
          val = []
        dr = $datepicker.data('datepicker')
        dp = dr.pickers
        if val.length == 2 && dp.length == 2
          dp[0].setDate(val[0])
          dp[1].setDate(val[1])
        dr.updateDates()
      else
        val = ko.utils.unwrapObservable(value)
        if val == null
          val = ''
        $datepicker.datepicker('setDate', val)
  })
