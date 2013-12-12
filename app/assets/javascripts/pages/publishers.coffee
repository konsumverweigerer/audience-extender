require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc", 
"lib/knockout-editable", 
"lib/knockout-jqbootstrapvalidation", 
"lib/knockout-datepicker", 
"lib/knockout-datatables", 
"jsRoutes" ], (ko, mod) ->
  class Publishers
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable ["name","admins"]

      @publishers = ko.observableArray []

      @currentpublisher = ko.observable(new mod.Publisher {name:'',id:-1})

      @selectpublisher = (p) ->
        {}

  models = new Publishers
    
  ko.applyBindings models
  
  window.models = models
  #init

  $(document).ready ->
    data.publishers?.map (p,i) ->
      pm = new mod.Publisher p
      models.publishers.push pm

  models.datatable.data models.publishers()

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'publisher')
  )
)
