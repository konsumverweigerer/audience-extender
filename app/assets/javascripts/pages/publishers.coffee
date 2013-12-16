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

      @selectpublisher = (a) ->
        self.currentpublisher (new mod.Publisher()).copyFrom(c)
        $('#editPublisher').modal 'show'

      @savepublisher = ->
        a = self.currentpublisher()
        l = self.datatable.data
        if a.id() && a.id()>0
          a.save(self)
          l.remove byId a.id()
          l.push a
        else
          a.save(self)
          l.push a
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

  require(["lib/data"],(demo) ->
    demo.generate(mod,models,'publisher')
    models.datatable.data models.publishers()
  )
)
