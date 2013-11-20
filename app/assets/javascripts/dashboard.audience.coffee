require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!d3.v2.js", "webjars!bootstrap.js",
"ext/jquery.jcarousel", "ext/bootstrap-slider", "lib/knockout-editable", "lib/knockout-datepicker", "lib/knockout-nvd3", "lib/knockout-datatables",
"/routes.js"], (ko, mod) ->
  class AudienceDashboard
    constructor: (d) ->
      self = @

      @audiencechartdaterange = new mod.DateRange

      @audiencetablescroller = new mod.Scroller

      @audiencetablesearchbar = new mod.Searchbar

      @audiencechart = new mod.Chartdata

      @audiencetable = new mod.Datatable(["name","state","revenue","cost","from","to"],
      {state: (v) ->
        if 'paused' == v
          '<span class="label label-default"><span class="glyphicon glyphicon-pause"></span> Paused</span>'
        else if 'finished' == v
          '<span class="label label-primary"><span class="glyphicon glyphicon-flag"></span> Finished</span>'
        else if 'active' == v
          '<span class="label label-success"><span class="glyphicon glyphicon-play"></span> Active</span>'
        else if 'pending' == v
          '<span class="label label-info"><span class="glyphicon glyphicon-time"></span> Pending</span>'
        else if 'cancelled' == v
          '<span class="label label-warning"><span class="glyphicon glyphicon-ban-circle"></span> Cancelled</span>'
        else if 'rejected' == v
          '<span class="label label-danger"><span class="glyphicon glyphicon-warning-sign"></span> Rejected</span>'
        else
          v
      })

      @publisher = ko.observable()

      @publishers = ko.observableArray([])

      @websites = ko.observableArray([])

      @minWebsite = ko.observable(1)

      @websiteCount = ko.observable(4)

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

  require(["webjars!nv.d3.js"], () ->
    data = ->
      m = (models.audiencechartdaterange.endDate()-models.audiencechartdaterange.startDate())/(24*60*60*1000)
      sd = models.audiencechartdaterange.startDate().getTime()
      idxf = (i) ->
        if m==1
          i = sd+(i*60*60*1000)
        else
          i = sd+(i*24*60*60*1000)
      tf = 'days'
      if m==1
        m = 24
        tf = 'hours'
      return stream_layers(3,m,.1,idxf).map( (data, i) ->
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

    stream_index = (d, i, idxf) ->
      {x: idxf(i), y: 100*Math.max(0, d)}

    stream_layers = (n, m, o, idxf) ->
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
          return a.map((r, s) ->
            stream_index(r, s, idxf))
      );

    models.audiencechartdaterange.dataloader = () ->
      models.audiencechart.charts(data())
    models.audiencechartdaterange.dateRange('Last Day')

    models.audiencetablesearchbar.filldata = () ->
      i = 0
      n = new Date()
      m = 40+Math.ceil(100*Math.random())
      val = []
      while i < m
        d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
        val[i++] = ['Name '+Math.ceil(1000*Math.random()),['paused','finished','active','pending','cancelled','rejected','40%'][(Math.floor(7*Math.random()))],
          '$'+(100*Math.random()).toFixed(2),'$'+(10*Math.random()).toFixed(2),mod.datetostr(d[0]),mod.datetostr(d[1])]
      models.audiencetable.data(val)
  )
)
