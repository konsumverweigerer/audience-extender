require([ "knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-misc", 
"lib/knockout-carousel", 
"lib/knockout-editable", 
"lib/knockout-jqbootstrapvalidation", 
"lib/knockout-datepicker", 
"lib/knockout-nvd3", 
"lib/knockout-zeroclipboard", 
"lib/knockout-datatables", 
"jsRoutes" ], (ko, mod) ->
  class AudienceDashboard
    constructor: (d) ->
      self = @

      byId = (id) -> ((w) -> w.id()==id)

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []
      @credential = ko.observable()

      @audiencechartdaterange = new mod.DateRange
      @audiencechart = new mod.Chartdata

      @audiencetablescroller = new mod.Scroller
      @audiencetablesearchbar = new mod.Searchbar {
        availableCategories: ['Any','Active','Not yet active','Deleted']
        categoryTags:
          active: 'Active'
          pending: 'Not yet active'
          cancelled: 'Deleted'
        categoryFilter: 'state'
        searchFilter: 'name'
      }
      @audiencetable = new mod.Datatable(["name","state","websiteNamesShort","count"],
        state: (v) ->
          if 'active' == v || 'A' == v
            '<span class="label label-success"><span class="glyphicon glyphicon-play"></span> Active</span>'
          else if 'pending' == v || 'P' == v
            '<span class="label label-info"><span class="glyphicon glyphicon-time"></span> Not yet active</span>'
          else if 'cancelled' == v || 'C' == v
            '<span class="label label-warning"><span class="glyphicon glyphicon-ban-circle"></span> Cancelled</span>'
          else
            v
        count: (v) ->
          '<span class="badge">'+v+'</span>'
      )

      @confirmaudiencedelete = ko.observable(0)
      @confirmwebsitedelete = ko.observable(0)

      @publisher = ko.observable()
      @publishers = ko.observableArray []

      @websites = ko.observableArray []
      @audiences = ko.observableArray []

      @currentaudience = ko.observable(new mod.Audience {name:'',id:-1})
      @currentwebsite = ko.observable(new mod.Website {name:'',id:-1})
      @websitetodelete = ko.observable()

      @currentwebsites = ko.observableArray []
      @websiteposition = new mod.Counter {wrap:false,minValue:0}

      @newwebsite = ->
        if self.confirmwebsitedelete()>0
          return
        self.currentwebsite(new mod.Website {name:'New Website',id:0})
        (v.active false; v.editing false) for v in self.currentwebsites()
        self.currentwebsite().active true
        self.currentwebsite().editing true

      @newaudience = ->
        self.confirmwebsitedelete 0
        self.confirmaudiencedelete 0
        self.currentaudience(new mod.Audience {name:'New Audience',id:0})
        self.currentwebsite(new mod.Website {name:'',id:-1})
        $('#editAudience').modal 'show'
        au = self.currentaudience()
        self.currentwebsites (w.refreshSelf(au).active(false).editing(false) for w in self.websites())
        self.websiteposition.maxValue self.websites().length
        self.websiteposition.currentValue ''

      @clearaudience = ->
        self.currentaudience(new mod.Audience {name:'',id:-1})
        $('#editAudience').modal 'hide'

      @clearwebsite = ->
        self.currentwebsite(new mod.Website {name:'',id:-1})

      @cleardeleteaudience = ->
        self.confirmaudiencedelete 0

      @deleteaudience = ->
        if self.confirmaudiencedelete()==0
          return self.confirmaudiencedelete 1
        self.currentaudience().remove(self)
        self.currentaudience(new mod.Audience {name:'',id:-1})
        $('#editAudience').modal 'hide'

      @saveaudience = ->
        a = self.currentaudience()
        self.currentaudience().refresh self.currentwebsites()
        l = self.audiencetable.data
        if a.id() && a.id()>0
          a.save(self)
          l.remove byId a.id()
          l.push a
        else
          a.save(self)
          l.push a
        self.currentaudience(new mod.Audience {name:'',id:-1})
        $('#editAudience').modal 'hide'

      @savewebsite = ->
        a = self.currentwebsite()
        l = self.websites
        if a.id() && a.id() > 0
          a.save(self)
          l.remove byId a.id()
          l.push a
          self.currentwebsite(new mod.Website {name:'',id:-1})
        else
          a.save(self)
          l.push a
        au = self.currentaudience()
        self.currentwebsites.push a
        w.refreshSelf(au).active(false).editing(false) for w in self.currentwebsites()
        self.currentaudience().refresh self.currentwebsites()
        self.websiteposition.maxValue self.currentwebsites().length
        self.websiteposition.currentValue 'last'
        self.editwebsite a

      @selectaudience = (c) ->
        self.confirmwebsitedelete 0
        self.confirmaudiencedelete 0
        if c.state()=='cancelled'
          self.alert.show('Edit Audience','Can\'t edit cancelled audiences.','warning')
          return
        self.currentaudience (new mod.Audience()).copyFrom(c)
        self.currentwebsite(new mod.Website {name:'',id:-1})
        $('#editAudience').modal 'show'
        au = self.currentaudience()
        self.currentwebsites (w.refreshSelf(au).active(false).editing(false) for w in self.websites())
        self.currentaudience().refresh self.currentwebsites()
        self.websiteposition.maxValue self.currentwebsites().length
        self.websiteposition.currentValue ''
        
      @cleardeletewebsite = ->
        self.confirmwebsitedelete 0

      @deletewebsite = (c) ->
        if c.websitetodelete
          c = c.websitetodelete()
        if self.confirmwebsitedelete()==1 or not c?
          return
        (v.active false; v.editing false) for v in self.currentwebsites()
        self.currentwebsite(new mod.Website {name:'',id:-1})
        self.websitetodelete c
        self.confirmwebsitedelete 1

      @confirmdeletewebsite = ->
        c = self.websitetodelete()
        if not c?
          return
        if self.confirmwebsitedelete()==0
          return
        #check for usage
        id = self.websitetodelete().id()
        self.websites.remove byId id
        self.currentwebsites.remove byId id
        self.currentwebsite().remove(self)
        #todo: speed up refresh
        #au.refresh self.currentwebsites() for au in self.audiences()
        self.confirmwebsitedelete 0
        self.websitetodelete undefined
        self.websiteposition.currentValue ''

      @activatewebsite = (c,e) ->
        if self.currentwebsite().editing()
          return
        if self.confirmwebsitedelete()>0
          return
        if not c.active()
          (v.active false; v.editing false) for v in self.currentwebsites()
          c.active true
        else
          self.selectwebsite c
        self.currentaudience().activewebsite c.id()
        self.currentwebsite(c)

      @selectwebsite = (c,e) ->
        e?.stopPropagation()
        if self.confirmwebsitedelete()>0
          return
        if c.selected()
          c.selected false
          self.currentaudience().websites.remove c.id()
        else
          c.selected true
          self.currentaudience().websites.push c.id()

      @editwebsite = (c,e) ->
        e?.stopPropagation()
        if self.confirmwebsitedelete()>0
          return
        nw = (new mod.Website()).copyFrom(c)
        nw.editing true
        nw.active true
        self.currentwebsite nw
        if not c.active()
          (v.active false; v.editing false) for v in self.currentwebsites()
          c.active true
        c.editing true
        self.currentwebsite().active true
        self.currentaudience().activewebsite c.id()

      @currentaudiencemessages = ko.computed
        read: ->
          self.currentaudience().messages()
        owner: self.currentaudience().messages

      @currentwebsitemessages = ko.computed
        read: ->
          self.currentwebsite().messages()
        owner: self.currentwebsite().messages

      @currentmessages = ko.computed
        read: ->
          a = []
          a = a.concat self.currentaudiencemessages()
          a = a.concat self.currentwebsitemessages()
          return a

  models = new AudienceDashboard

  ko.applyBindings models

  # todo: via ko
  models.audiencetable.rowClick = (c) ->
    models.selectaudience c

  window.models = models
  #init

  $(document).ready ->
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

  require(["lib/data"],(dat) ->
    dat.generate(mod,models,'audience')
  )
  
  require(["lib/demodata"],(demo) ->
    demo.generate(mod,models,'audience')
    models.audiencechartdaterange.dateRange 'Last Day'
    models.audiencetablesearchbar.filldata = ->
      models.audiencetable.data models.audiencetablesearchbar.filter models.audiences()
    models.audiencetablesearchbar.datatable models.audiencetable
    models.audiencetablesearchbar.search()
  )
)
