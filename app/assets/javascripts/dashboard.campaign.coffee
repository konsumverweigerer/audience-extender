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

      @selectcampaign = (c) ->
        self.currentcampaign((new mod.Campaign()).copyFrom(c))
        $('#editCampaign').modal()

  models = new CampaignDashboard

  ko.applyBindings(models)

  models.campaigntable.rowClick = (c) ->
    models.selectcampaign(c)

  window.models = models
  #init

  models.publishers().map((p,i) ->
    pm = new mod.Publisher p
    models.publishers.push pm
    if p.active == "true"
      models.publisher pm
  )
  if !models.publisher() && models.publishers().length
      models.publisher models.publishers()[0]

  require(["lib/demodata"],(demo) ->
    demo.generate(mod,models,'campaign')
  )
)
