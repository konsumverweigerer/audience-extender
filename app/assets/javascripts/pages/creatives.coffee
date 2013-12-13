require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc", 
"lib/knockout-editable", 
"lib/knockout-jqbootstrapvalidation", 
"lib/knockout-datepicker", 
"lib/knockout-datatables", 
"jsRoutes" ], (ko, mod) ->
  class Creatives
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable ["name"]

      @creatives = ko.observableArray []

      @publisher = ko.observable()
      @publishers = ko.observableArray []

      @currentcreative = ko.observable(new mod.Creative {name:'',id:-1})

      @selectcreative = (c) ->
        {}

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
    if !models.publisher() && models.publishers().length
      p = models.publishers()[0]
      p.active 'true'
      models.publisher p

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
