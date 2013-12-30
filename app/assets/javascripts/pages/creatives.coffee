require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc",
"lib/knockout-editable",
"lib/knockout-jqbootstrapvalidation",
"lib/knockout-datepicker",
"lib/knockout-datatables",
"lib/knockout-noty",
"jsRoutes" ], (ko, mod) ->
  class Creatives
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []
      @credential = ko.observable()

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable(["name", "state", "variant"],
        state: (v) ->
          if 'active' == v || 'A' == v
            '<span class="label label-success"><span class="glyphicon glyphicon-play"></span> Active</span>'
          else if 'pending' == v || 'P' == v
            '<span class="label label-info"><span class="glyphicon glyphicon-time"></span> Not yet active</span>'
          else if 'cancelled' == v || 'C' == v
            '<span class="label label-warning"><span class="glyphicon glyphicon-ban-circle"></span> Cancelled</span>'
          else
            v
      )

      @creatives = ko.observableArray []

      @publisher = ko.observable()
      @publishers = ko.observableArray []

      @currentcreative = ko.observable(new mod.Creative {name:'',id:-1})

      @selectcreative = (c) ->
        self.currentcreative (new mod.Creative()).copyFrom c
        $('#editCreative').modal 'show'

      @savecreative = ->
        c = self.currentcreative()
        l = self.datatable.data
        if c.id() && c.id()>0
          c.save self
          l.remove byId c.id()
          l.push c
        else
          c.save self
          l.push c
        self.currentcreative(new mod.Creative {name:'',id:-1})
        $('#editCreative').modal 'hide'

  models = new Creatives
    
  ko.applyBindings models
  
  window.models = models
  #init

  $(document).ready ->
    data.creatives?.map (c,i) ->
      cm = new mod.Creative c
      models.creatives.push cm
    data.publishers?.map (p,i) ->
      pm = new mod.Publisher p
      models.publishers.push pm
      if p.active == 'true'
        models.publisher pm
    if !models.publisher() && models.publishers().length
      p = models.publishers()[0]
      p.active 'true'
      models.publisher p
    models.credential(new mod.Admin data.admin)

  models.publisher.subscribe (nv) ->
    a = routes.controllers.AdminController.changePublisher nv.id()
    a.ajax {
      success: (nv) ->
        for p in models.publishers()
          if nv.id==p.id()
            p.active 'true'
          else
            p.active 'false'
      error: ->
        models.alert.show('Warning','Could not change publisher','error')
    }

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'creative')
    models.datatable.data models.creatives()
  )
)
