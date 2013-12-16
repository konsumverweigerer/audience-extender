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
        self.currentadmin (new mod.Admin()).copyFrom(c)
        $('#editAdmin').modal 'show'

      @saveadmin = ->
        a = self.currentadmin()
        l = self.datatable.data
        if a.id() && a.id()>0
          a.save(self)
          l.remove byId a.id()
          l.push a
        else
          a.save(self)
          l.push a
        self.currentadmin(new mod.Admin {name:'',id:-1})
        $('#editAdmin').modal 'hide'

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
