require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc", 
"lib/knockout-editable", 
"lib/knockout-jqbootstrapvalidation", 
"lib/knockout-datepicker", 
"lib/knockout-datatables", 
"jsRoutes" ], (ko, mod) ->
  class Admins
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable ["name","email","roles","publishers"]

      @admins = ko.observableArray []

      @selectadmin = (a) ->
        {}

  models = new Admins
    
  ko.applyBindings models
  
  window.models = models
  #init

  if window.data && window.data.admins
    for a in window.data.admins
      ad = new mod.Admin a
      models.admins.push ad
  models.datatable.data models.admins()

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'admin')
  )
)
