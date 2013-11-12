require(["webjars!jquery.js", "webjars!d3.v2.js", "webjars!knockout.js", "/routes.js"], (jq, dd, ko) ->
  truncateToDay = (d,s,e,u) ->
    base = new Date(d.getFullYear(),d.getMonth(),d.getDate())
    day = 24*60*60*1000
    if u && u == 'week'
      dw = base.getDay()
      s = (7-dw)-(7*s)
      e = (7-dw)-(7*e)
      from = new Date(base.getTime()+(s*day))
      to = new Date(base.getTime()+(e*day))
    else
      from = new Date(base.getTime()+((1-s)*day))
      to = new Date(base.getTime()+((1-e)*day))
    [from,to]
  
  datetostr = (v) ->
    (v.getMonth()+1)+"/"+v.getDate()+"/"+v.getFullYear()
  	
  strtodate = (s) ->
    v = s.split('/')
    new Date(v[2],v[0]-1,v[1])

  class DateRange
    constructor: () ->
      self = @
      
      @availableDateRanges = ko.observableArray(['','Last Day','Last Week'])
      
      @startDate = ko.observable(truncateToDay(new Date(),2,1)[0])
      
      @endDate = ko.observable(truncateToDay(new Date(),2,1)[1])
      
      @formattedStartDate = ko.computed({
        read: () ->
          datetostr(self.startDate())
        write: (v) ->
          old = self.startDate()
          self.startDate(strtodate(v))
          if old!=self.startDate()
            self.loadData()
        owner: self
      })

      @formattedEndDate = ko.computed({
        read: () ->
          datetostr(self.endDate())
        write: (v) ->
          old = self.endDate()
          self.endDate(strtodate(v))
          if old!=self.endDate()
            self.loadData()
        owner: self
      })
      
      @dateRange = ko.computed({
      	read: () ->
      	  lastDay = truncateToDay(new Date(),2,1)
      	  if lastDay[0]==self.startDate && lastDay[1]==self.endDate
      	    return 'Last Day' 
      	  lastWeek = truncateToDay(new Date(),2,1,'week')
      	  if lastWeek[0]==self.startDate && lastWeek[1]==self.endDate
      	    return 'Last Week' 
      	  return ''
      	write: (v) ->
      	  if v == 'Last Day'
            self.lastDay()
            self.loadData()
      	  if v == 'Last Week'
            self.lastWeek()
            self.loadData()
      	owner: self
      })
      
      @lastDay = () ->
        t = truncateToDay(new Date(),2,1)
        self.startDate(t[0])
        self.endDate(t[1])
      
      @lastWeek = () ->
        t = truncateToDay(new Date(),2,1,'week')
        self.startDate(t[0])
        self.endDate(t[1])
        
      @loadData = () ->
        self.dataloader()
      
      @dataloader = () ->
        {}
        
  chartdaterange = new DateRange
  ko.applyBindings(chartdaterange)
  chartdaterange.loadData()
  require(["webjars!nv.d3.js"], () ->
    data = ->
      m = (chartdaterange.endDate()-chartdaterange.startDate())/(24*60*60*1000)
      tf = 'days'
      if m==1
        m = 24
        tf = 'hours'
      return stream_layers(3,m,.1).map( (data, i) ->
        if i==0
          s = 'Ad spend'
        if i==1
          s = 'Revenue'
        if i==2
          s = 'Profit'
        return {
          key: s,
          values: data,
          timeframe: tf 
        }
      )

    stream_index = (d, i) ->
      m = (chartdaterange.endDate()-chartdaterange.startDate())/(24*60*60*1000)
      if m==1
        i = chartdaterange.startDate().getTime()+(i*60*60*1000)
      else
        i = chartdaterange.startDate().getTime()+(i*24*60*60*1000)
      {x: i, y: 100*Math.max(0, d)}
         
    stream_layers = (n, m, o) -> 
      if arguments.length < 3
        o = 0    
      bump = (a) ->
        x = 1 / (.1 + Math.random())
        y = 2 * Math.random() - .5
        z = 10 / (.1 + Math.random())
        i = 0
        while i < m
          w = (i / m - y) * z
          a[i++] += x * Math.exp(-w * w)
      return d3.range(n).map( () ->
          a = []
          i = 0
          while i < m
            a[i++] = o + o * Math.random()
          i = 0
          while i++ < 5
            bump(a)
          return a.map(stream_index)
      );
    chartdaterange.dataloader = () ->
      nv.addGraph(() ->
        chart = nv.models.lineChart()
        currentdata = data()
        if currentdata.length > 0 && currentdata[0].timeframe == 'days'
          chart.xAxis
            .tickFormat((d) ->
              d3.time.format('%m/%d/%y')(new Date(d))
            );
        else
          chart.xAxis
            .tickFormat((d) ->
              d3.time.format('%H:%M')(new Date(d))
            );
        chart.yAxis
          .tickFormat((d) -> 
            '$'+(d3.format('.2f'))(d)
          );
        d3.select('#chart svg')
          .datum(currentdata)
          .transition().duration(500).call(chart)
        nv.utils.windowResize(chart.update)
        return chart
      )
  )
  $('input.date-picker').on('changeDate',(v) ->
    if v.target.name == 'start'
      chartdaterange.formattedStartDate(datetostr(v.date))
    if v.target.name == 'end'
      chartdaterange.formattedEndDate(datetostr(v.date))
  )
)

require(["webjars!jquery.js", "webjars!bootstrap.js"], () ->
  require(["webjars!bootstrap-datepicker.js"], () ->
    $('.input-daterange').datepicker({
      calendarWeeks: true,
      autoclose: true,
      todayHighlight: true
    });
  )
)
