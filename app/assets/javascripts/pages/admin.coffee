require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc", 
"lib/knockout-editable", 
"lib/knockout-jqbootstrapvalidation", 
"lib/knockout-datepicker", 
"lib/knockout-datatables", 
"jsRoutes" ], (ko, mod) ->
  class Admin
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []
      @credential = ko.observable()

      @currentadmin = ko.observable(new mod.Admin {name:'',id:-1})

      @saveadmin = ->
        a = self.currentadmin()
        if a.id() && a.id()>0
          a.save self
        else
          a.save self

  models = new Admin
    
  ko.applyBindings models
  
  window.models = models
  #init

  $(document).ready ->
    data.admins?.map (a,i) ->
      am = new mod.Admin a
      models.currentadmin am
    models.credential(new mod.Admin data.admin)
)
