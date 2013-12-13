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

      @publisher = ko.observable()
      @publishers = ko.observableArray []

      @currentadmin = ko.observable(new mod.Admin {name:'',id:-1})

      @selectadmin = (a) ->
        {}

  models = new Admins
    
  ko.applyBindings models
  
  window.models = models
  #init

  $(document).ready ->
    data.admins?.map (a,i) ->
      am = new mod.Admin a
      models.admins.push am

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'admin')
    models.datatable.data models.admins()
  )
)
