define("jquery", [ "webjars!jquery.js" ], () ->
  $
)

require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!d3.v2.js", "webjars!bootstrap.js",
"lib/knockout-misc", "lib/knockout-editable", "lib/knockout-datepicker", "lib/knockout-nvd3", 
"lib/knockout-datatables", "/routes.js"], (ko, mod) ->
  class AudienceDashboard
    constructor: (d) ->
      self = @

      @audiencechartdaterange = new mod.DateRange

      @audiencetablescroller = new mod.Scroller

      @audiencetablesearchbar = new mod.Searchbar

      @audiencechart = new mod.Chartdata

      @audiencetable = new mod.Datatable(["name","state","websites","count"],
      {state: (v) ->
        if 'paused' == v
          '<span class="label label-default"><span class="glyphicon glyphicon-pause"></span> Paused</span>'
        else if 'active' == v
          '<span class="label label-success"><span class="glyphicon glyphicon-play"></span> Active</span>'
        else if 'pending' == v
          '<span class="label label-info"><span class="glyphicon glyphicon-time"></span> Pending</span>'
        else if 'cancelled' == v
          '<span class="label label-warning"><span class="glyphicon glyphicon-ban-circle"></span> Cancelled</span>'
        else
          v
      })

      @messages = ko.observableArray([])

      @publisher = ko.observable()

      @publishers = ko.observableArray([])

      @websites = ko.observableArray([])

      # dummy for init
      @currentaudience = ko.observable(new mod.Audience({name:'',id:-1}))

      # dummy for init
      @currentwebsite = ko.observable(new mod.Website({name:'',id:-1}))

      @websiteposition = new mod.Counter()

      @newwebsite = () ->
        self.currentwebsite(new mod.Website({name:'New Website',id:0}))

      @newaudience = () ->
        self.currentaudience(new mod.Audience({name:'New Audience',id:0}))
        self.currentwebsite(new mod.Website({name:'',id:-1}))

      @saveaudience = () ->
        a = self.currentaudience()
        l = self.audiencetable.data()
        if a.id() && a.id()>0
          alert('update audience')
          l.remove((b) ->
            a.id() == b.id()
          )
          l.push(a)
        else
          alert('persist audience')
          l.push(a)

      @savewebsite = () ->
        a = self.currentwebsite()
        l = self.websites()
        if a.id() && a.id() > 0
          alert('update website')
          l.remove((b) ->
            a.id() == b.id()
          )
          l.push(a)
        else
          alert('persist website')
          l.push(a)

      @selectaudience = (c) ->
        self.currentaudience((new mod.Audience()).copyFrom(c))
        self.currentwebsite(new mod.Website({name:'',id:-1}))
        $('#editAudience').modal()
        
      @selectwebsite = (c) ->
        alert(c)

  models = new AudienceDashboard

  ko.applyBindings(models)

  # todo: via ko
  models.audiencetable.rowClick = (c) ->
    models.selectaudience(c)

  window.models = models
  #init

  models.publishers.map((p,i) ->
    pm = new mod.Publisher p
    models.publishers.push pm
    if p.active == "true"
      models.publisher pm
  )
  if !models.publisher() && models.publishers().length
  	models.publisher models.publishers()[0]

  require(["lib/demodata"],(demo) ->
    demo.generate(mod,models,'audience')
  )
)
