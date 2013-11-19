require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!d3.v2.js", "webjars!bootstrap.js", 
"ext/jquery.jcarousel", "lib/knockout-editable", "lib/knockout-datepicker", "lib/knockout-nvd3", "lib/knockout-datatables", 
"/routes.js"], (ko, mod) ->
  class AudienceDashboard
    constructor: (d) ->
      self = @

      @audiencechartdaterange = new mod.DateRange

      @audiencetablescroller = new mod.Scroller

      @audiencetablesearchbar = new mod.Searchbar

      @audiencechartdata = new mod.Chartdata

      @audiencedatatable = new mod.Datatable(["name","state","revenue","cost","from","to"])

      @publisher = ko.observable()

      @publishers = ko.observableArray([])

      @websites = ko.observableArray([])

      @minWebsite = ko.observable(1)

      @websiteCount = ko.observable(4)

      @visibleWebsites = ko.computed(() ->
        ii = Math.max(self.minWebsite(),self.websites().length+1)
        ai = Math.min(self.minWebsite()+self.websiteCount()-1,self.websites().length+1)
        self.websites().filter((n,i) ->
          (i + 1) >= ii && (i + 1) <= ai
        )
      )

      @prevWebsite = () ->
        n = self.minWebsite()
        if n > 2
          self.minWebsite(n - 1)

      @nextWebsite = () ->
        n = self.minWebsite()
        m = self.websiteCount()
        if (n + m - 1) < self.websites().length
          self.minWebsite(n + 1)

      # dummy for init
      @currentaudience = ko.observable(new mod.Audience({name:''}))

      # dummy for init
      @currentwebsite = ko.observable(new mod.Website({name:''}))

      @messages = ko.observableArray([])

      @newaudience = () ->
        self.currentaudience(new mod.Audience({name:'New Audience'}))

      @newwebsite = () ->
        self.currentwebsite(new mod.Website({name:'New Website'}))

      @saveaudience = () ->
        a = self.currentaudience()
        if a.id()
          alert('update audience')
        else
          alert('persist audience')

      @savewebsite = () ->
        a = self.currentwebsite()
        if a.id()
          alert('update website')
        else
          alert('persist website')

  models = new AudienceDashboard
    
  ko.applyBindings(models)
  
  window.models = models
  #init

  window.data.publishers.map( (p,i) ->
    pm = new mod.Publisher(p)
    models.publishers.push(pm)
    if p.active == "true"
      models.publisher(pm)
  )
  if !models.publisher() && models.publishers().length
  	models.publisher(models.publishers()[0])

  models.messages.push(new mod.Message('Kampagne MegaAudiences läuft bald aus',
        'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, 
        sed diam nonumy eirmod tempor invidunt ut labore et dolore 
        magna aliquyam erat, sed diam voluptua. At vero eos et accusam 
        et justo duo dolores et ea rebum. Stet clita kasd gubergren, 
        no sea takimata sanctus est Lorem ipsum dolor sit amet.','info'))
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
          s = 'Ad Spend'
          t = 'adspend'
        if i==1
          s = 'Revenue'
          t = 'revenue'
        if i==2
          s = 'Profit'
          t = 'profit'
        return {
          key: s,
          cls: t,
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
      models.chartdaterange.dateRange('Last Day')
    )
    models.datatablesearchbar.filldata = () ->
      i = 0
      n = new Date()
      m = 40+Math.ceil(100*Math.random())
      val = []
      while i < m
        d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
        val[i++] = ['Name '+Math.ceil(1000*Math.random()),(100*Math.random()).toFixed(1)+'%',
          '$'+(100*Math.random()).toFixed(2),'$'+(10*Math.random()).toFixed(2),mod.datetostr(d[0]),mod.datetostr(d[1])]
      models.datatable.data(val)
  )
)
