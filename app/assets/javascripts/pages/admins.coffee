require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc",
"lib/knockout-editable",
"lib/knockout-jqbootstrapvalidation",
"lib/knockout-datepicker",
"lib/knockout-datatables",
"lib/knockout-noty",
"jsRoutes" ], (ko, mod) ->
  class Admins
    constructor: (d) ->
      self = @

      byId = (id) -> ((w) -> (ko.unwrap w.id)==id)

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []
      @credential = ko.observable()

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable(["name","email","roles","publishers"],
        publishers: (v) ->
          l = (p.name() for p in v)
          n = l.join ', '
          if n.length>30
            return n.substr(0,28)+' ...'
          return n
      )

      @admins = ko.observableArray []

      @publisher = ko.observable()
      @publishers = ko.observableArray []

      @currentadmin = ko.observable(new mod.Admin {name:'',id:-1})

      @clearadmin = ->
        self.currentadmin(new mod.Admin {name:'',id:-1})
        $('#editAdmin').modal 'hide'

      @selectadmin = (a) ->
        self.currentadmin (new mod.Admin()).copyFrom a
        $('#editAdmin').modal 'show'

      @addpublisher = (p) ->
        self.currentadmin.addpublisher(p,self.messages)

      @delpublisher = (p) ->
        self.currentadmin.delpublisher(p,self.messages)

      @saveadmin = ->
        a = self.currentadmin()
        l = self.admins
        if a.id() && a.id()>0
          a.save(self, ->
            l.remove byId a.id()
            l.push a
          )
        else
          a.save(self, ->
            l.push a
          )
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
    models.credential(new mod.Admin data.admin)

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'admin')
    models.datatable.data models.admins()
  )
)
