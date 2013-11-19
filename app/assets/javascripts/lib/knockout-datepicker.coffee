define([ "webjars!knockout.js", "webjars!bootstrap-datepicker.js", "webjars!jquery.js" ], (ko) ->
  ko.bindingHandlers.datepicker = {
    init : (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()
      datepickerOptions = allBindings.datepickerOptions || {}

      $datepicker = $element.datepicker(datepickerOptions)

      if ko.isObservable(value)
        if $datepicker.is('.input-daterange')
          $dp = $datepicker.data('datepicker').pickers
          $dp[0].on('changeDate.ko', (e, params) ->
            v = valueAccessor()()
            valueAccessor().dates([params.newValue,v[1]])
          )
          $dp[1].on('changeDate.ko', (e, params) ->
            v = valueAccessor()()
            valueAccessor().dates([v[0],params.newValue])
          )
        else
          $datepicker.on('changeDate.ko', (e, params) ->
            valueAccessor()(params.newValue)
          )

      if datepickerOptions.changeData
        $datepicker.on('changeDate', datepickerOptions.changeDate)

    , update : (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
      $element = $(element)
      value = valueAccessor()
      allBindings = allBindingsAccessor()

      $datepicker = $element.datepicker()

      if $datepicker.is('.input-daterange')
        val = ko.utils.unwrapObservable(valueAccessor()).dates()
        if val == null
          val = [];
        $dp = $datepicker.data('datepicker').pickers
        if val.length == 2 && $dp.length == 2
          $dp[0].dadatepicker('setDate', val[0])
          $dp[1].dadatepicker('setDate', val[1])
      else
        val = ko.utils.unwrapObservable(valueAccessor())
        if val == null
          val = ''
        $datepicker.datepicker('setDate', val)
  })
