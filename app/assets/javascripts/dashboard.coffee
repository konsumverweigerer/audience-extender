require(["webjars!jquery.js", "webjars!d3.v2.js", "webjars!knockout.js", "webjars!bootstrap.js", "/routes.js"], (jq, dd, ko) ->
  day = 24*60*60*1000

  ranges = [{ name: 'Last Day', from: 2, to: 1, unit: 'day' },
     { name: 'This Week', from: 1, to: 0, unit: 'week' },
     { name: 'Last Week', from: 2, to: 1, unit: 'week' }, 
     { name: 'Last 2 Weeks', from: 3, to: 1, unit: 'week' }]

  truncateToDay = (d,s,e,u) ->
    base = new Date(d.getFullYear(),d.getMonth(),d.getDate())
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

  rangeNames = () ->
    a = ['']
    ranges.map( (n,i) ->
      a.push(n.name)
    )
    return a

  class DateRange
    constructor: () ->
      self = @
      
      @availableDateRanges = ko.observableArray(rangeNames())
      
      @startDate = ko.observable(truncateToDay(new Date(),2,1)[0])
      
      @endDate = ko.observable(truncateToDay(new Date(),2,1)[1])
      
      @format = ko.computed(()->
        if self.endDate().getTime()-self.startDate().getTime() < 2*day
          return '%H:%M'
        return '%m/%d/%Y'
      )
  
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
      	  v = ''
      	  nd = new Date()
      	  sd = self.startDate()
      	  ed = self.endDate()
      	  ranges.map( (n,i)->
      	    t = truncateToDay(nd,n.from,n.to,n.unit)
      	    if Math.abs(t[0].getTime()-sd.getTime())<3600001 && Math.abs(t[1].getTime()-ed.getTime())<3600001
      	      v = n.name 
      	  )
      	  return v
      	write: (v) ->
          ranges.map( (n,i)->
            if v == n.name
              t = truncateToDay(new Date(),n.from,n.to,n.unit)
              self.startDate(t[0])
              self.endDate(t[1])
              self.loadData()
            return
          )
          return
      	owner: self,
      	deferEvaluation: true
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
        
  class Scroller
    constructor: () ->
      self = @
      
      @currentPage = ko.observable(1)

      @maxPage = ko.observable(1)

      @shownPages = ko.observable(5)

      @pageSize = ko.observable(10)

      @availablePages = ko.computed(() ->
        a = []
        i = 1
        m = self.maxPage()
        while i <= m
          a[i-1] = i++
        return a
      )

      @visiblePages = ko.computed(()->
        p = []
        i = 1
        mp = self.maxPage()
        sp = self.shownPages()
        cp = self.currentPage()
        while (i + (sp/2) - 1) < cp && (i + sp - 1) < mp
          i++
        j = 0
        while j < sp && i+j <= mp
          p[j] = {
            page: i+j,
            active: (i+j)==cp
          }
          j++
        return p
      )

      @previous = () ->
        c = self.currentPage()
        if c > 1
          self.currentPage(c-1)

      @next = () ->
        c = self.currentPage()
        mp = self.maxPage()
        if c < mp
          self.currentPage(c+1)

      @gotoPage = () ->
        self.currentPage(@page)
  
  class Searchbar
    constructor: () ->
      self = @

      @availableCategories = ko.observableArray(['Status 1','Status 2','Status 3'])
      
      @category = ko.observable('')

      @query = ko.observable('')
      
      @search = () ->
        self.filldata()
        
      @filldata = () ->
        {}

  class Chartdata
    constructor: () ->
      self = @

      @charts = ko.observableArray([
        {key: 'Revenue', values: [{x:0,y:5000}]},
        {key: 'Ad spend', values: [{x:0,y:4000}]},
        {key: 'Profit', values: [{x:0,y:3000}]}])

      @sums = ko.computed(() ->
        self.charts().map((n,i) ->
          n.values.map((p,j) -> 
            p.y
          ).reduce((x,y) ->
            x+y
          )
        )
      )

      @sum0 = ko.computed(() ->
        self.sums()[0].toFixed(0)
      )

      @sum1 = ko.computed(() ->
        self.sums()[1].toFixed(0)
      )

      @sum2 = ko.computed(() ->
        self.sums()[2].toFixed(0)
      )

  class Datatable
    constructor: () ->
      self = @

      @rows = ko.observableArray([])
      
  models = {
    chartdaterange: new DateRange,
    datatablescroller: new Scroller,
    datatablesearchbar: new Searchbar,
    chartdata: new Chartdata,
    datatable: new Datatable
  }
  
  ko.applyBindings(models)
  
  models.chartdaterange.loadData()
  require(["webjars!nv.d3.js"], () ->
    data = ->
      m = (models.chartdaterange.endDate()-models.chartdaterange.startDate())/(24*60*60*1000)
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
      m = (models.chartdaterange.endDate()-models.chartdaterange.startDate())/(24*60*60*1000)
      if m==1
        i = models.chartdaterange.startDate().getTime()+(i*60*60*1000)
      else
        i = models.chartdaterange.startDate().getTime()+(i*24*60*60*1000)
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
    require(["webjars!bootstrap-datepicker.js"], () ->
      $('.input-daterange').datepicker({
        calendarWeeks: true,
        autoclose: true,
        todayHighlight: true
      });
      models.chartdaterange.dataloader = () ->
        nv.graphs.pop()
        nv.addGraph(() ->
          currentdata = data()
          models.chartdata.charts(currentdata)
          chart = nv.models.lineChart()
          chart.xAxis.showMaxMin(false).staggerLabels(true)
            .tickFormat((d) ->
              d3.time.format(models.chartdaterange.format())(new Date(d))
            );
          chart.yAxis.showMaxMin(false)
            .tickFormat((d) -> 
              '$'+(d3.format('.2f'))(d)
            );
          chart.color(['#bf0c0c','#275980','#f3b300'])
          d3.select('#chart svg')
            .datum(currentdata)
            .transition().duration(500).call(chart)
          nv.utils.windowResize(chart.update)
          return chart
        )
      models.chartdaterange.dateRange.subscribe((nv)->
        $('input.date-picker[name=start]').datepicker('setDate',models.chartdaterange.startDate())
        $('input.date-picker[name=end]').datepicker('setDate',models.chartdaterange.endDate())
      )
      $('input.date-picker').on('changeDate',(v) ->
        if v.target.name == 'start'
          models.chartdaterange.formattedStartDate(datetostr(v.date))
        if v.target.name == 'end'
          models.chartdaterange.formattedEndDate(datetostr(v.date))
      )
      models.chartdaterange.dateRange('Last Day')
    )
    require(["webjars!jquery.dataTables.js"], () ->
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
      datatable = $('table.data-table').dataTable({
        bLengthChange: false,
        aoColumns: [
          { sType: "string" },
          { sType: "percent" },
          { sType: "currency" },
          { sType: "currency" },
          { sType: "date" },
          { sType: "date" }
        ],
        sDom: 'lrti',
        fnInfoCallback: (oSettings, iStart, iEnd, iMax, iTotal, sPre) ->
          $('.pagination-text').html('Displaying '+iStart+' - '+iEnd+' of '+iMax)
          dl = oSettings._iDisplayLength
          page = 1
          i = iStart
          while i-dl > 0
            i = i-dl
            page++ 
          models.datatablescroller.maxPage(Math.ceil(iMax/dl))
          return ''
      });
      models.datatablescroller.currentPage.subscribe((nv)->
        datatable.fnPageChange(nv-1)
      )
      models.datatablesearchbar.filldata = () ->
        datatable.fnClearTable()
        i = 0
        n = new Date()
        m = 40+Math.ceil(100*Math.random())
        while i++ < m
          d = truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
          datatable.fnAddData(['Name '+Math.ceil(1000*Math.random()),(100*Math.random()).toFixed(1)+'%',
          	'$'+(100*Math.random()).toFixed(2),'$'+(10*Math.random()).toFixed(2),datetostr(d[0]),datetostr(d[1])])
    )
  )
)
