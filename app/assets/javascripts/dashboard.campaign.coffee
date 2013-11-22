define("jquery", [ "webjars!jquery.js" ], () ->
  $
)

require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!d3.v2.js", "webjars!bootstrap.js",
"lib/knockout-misc", "lib/knockout-editable", "lib/knockout-datepicker", "lib/knockout-nvd3", 
"lib/knockout-datatables", "/routes.js"], (ko, mod) ->
  class CampaignDashboard
    constructor: (d) ->
      self = @

      @campaignchartdaterange = new mod.DateRange

      @campaigntablescroller = new mod.Scroller

      @campaigntablesearchbar = new mod.Searchbar

      @campaignchart = new mod.Chartdata

      @campaigntable = new mod.Datatable(["name","state","revenue","cost","from","to"],
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
      , revenue: (v) ->
        '$'+v.toFixed(2)
      , cost: (v) ->
        '$'+v.toFixed(2)
      , from: (v) ->
        mod.datetostr(v)
      , to: (v) ->
        mod.datetostr(v)
      })

      @publisher = ko.observable()

      @publishers = ko.observableArray([])

      @campaignstep = ko.observable(1)

      @currentcampaign = ko.observable(new mod.Campaign({name:''}))

      @messages = ko.observableArray([])

      @resetstep = () ->
        self.campaignstep(1)

      @nextstep = () ->
        self.campaignstep(self.campaignstep()+1)

      @prevstep = () ->
        self.campaignstep(self.campaignstep()-1)

      @newcampaign = () ->
        self.currentcampaign(new mod.Campaign({name:'New Campaign'}))

      @savecampaign = () ->
        a = self.currentcampaign()
        if a.id()
          alert('update campaign')
        else
          alert('persist campaign')

  models = new CampaignDashboard

  ko.applyBindings(models)

  models.campaigntable.rowClick = (c) ->
    models.currentcampaign(c)
    $('#editCampaign').modal()

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

  data = ->
    m = (models.campaignchartdaterange.endDate()-models.campaignchartdaterange.startDate())/(24*60*60*1000)
    sd = models.campaignchartdaterange.startDate().getTime()
    idxf = (i) ->
      if m<2
        i = sd+(i*60*60*1000)
      else
        i = sd+(i*24*60*60*1000)
    tf = 'days'
    mn = m
    if m<2
      mn = 24
      tf = 'hours'
    dat = stream_layers(2,mn,.1,idxf).map( (data, i) ->
      if i==0
        s = 'Revenue'
        t = 'revenue'
      else if i==1
        s = 'Ad Spend'
        t = 'adspend'
      return {
        key: s,
        cls: t,
        values: data.map((n,j) ->
          if i==1
            n.y = -n.y/5
          if tf=='hours'
            n.y = n.y/24
          n
        ),
        timeframe: tf
      }
    )
    dat[0].values[0].y=0;
    dat[1].values[0].y=0;
    dat[2] = {
    key: 'Profit',
    cls: 'profit',
    values: sumdata(dat[0].values,dat[1].values)
    timeframe: tf
    }
    dat

  sumdata = (v, w) ->
    if v.length==w.length
      v.map((n,i) ->
        {x:n.x,y:n.y+w[i].y}
      )
    else
      []
    
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

  models.campaignchartdaterange.dataloader = () ->
    models.campaignchart.chartcontent(data())

  models.campaignchartdaterange.dateRange('Last Day')
  
  models.campaigntablesearchbar.filldata = () ->
    i = 0
    n = new Date()
    m = 40+Math.ceil(100*Math.random())
    val = []
    while i < m
      d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
      val[i++] = new mod.Campaign({name: 'Campaign '+Math.ceil(1000*Math.random()),
      state: ['paused','finished','active','pending','cancelled','rejected',Math.floor(100*Math.random())+'%'][(Math.floor(7*Math.random()))],
      revenue: (100*Math.random()),
      cost: (10*Math.random()),
      from: d[0],
      to: d[1]})
    models.campaigntable.data(val)
)
