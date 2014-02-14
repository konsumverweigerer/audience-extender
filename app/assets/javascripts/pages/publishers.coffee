require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc",
"lib/knockout-editable",
"lib/knockout-jqbootstrapvalidation",
"lib/knockout-datepicker",
"lib/knockout-datatables",
"lib/knockout-noty",
"jsRoutes" ], (ko, mod) ->
  class Publishers
    constructor: (d) ->
      self = @

      byId = (id) -> ((w) -> (ko.unwrap w.id)==id)

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []
      @credential = ko.observable()

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable(["name","admins"],
        admins: (v) ->
          l = (a.name() for a in v)
          n = l.join ', '
          if n.length>30
            return n.substr(0,28)+' ...'
          return n
      )

      @publishers = ko.observableArray []

      @currentpublisher = ko.observable(new mod.Publisher {name:'',id:-1})

      @clearpublisher = ->
        self.currentpublisher(new mod.Publisher {name:'',id:-1})
        $('#editPublisher').modal 'hide'

      @selectpublisher = (p) ->
        self.currentpublisher (new mod.Publisher()).copyFrom p
        $('#editPublisher').modal 'show'

      @deladmin = (a) ->
        self.currentpublisher().admins.remove byId ko.unwrap a.id

      @savepublisher = ->
        p = self.currentpublisher()
        l = self.publishers
        if p.id() && p.id()>0
          p.save(self, ->
            l.remove byId p.id()
            l.push p
          )
        else
          p.save(self, ->
            l.push p
          )
        self.currentpublisher(new mod.Publisher {name:'',id:-1})
        $('#editPublisher').modal 'hide'

  models = new Publishers
    
  ko.applyBindings models
  
  window.models = models
  #init

  $(document).ready ->
    data.publishers?.map (p,i) ->
      pm = new mod.Publisher p
      models.publishers.push pm
    models.credential(new mod.Admin data.admin)

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'publisher')
    models.datatable.data models.publishers()
  )
)
