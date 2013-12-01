require([ "knockout", "lib/models", "jquery", "bootstrap"
"lib/knockout-misc", "lib/knockout-editable", "lib/knockout-datepicker", "lib/knockout-nvd3", "lib/knockout-datatables", 
"jsRoutes" ], (ko, mod) ->
  class CampaignDashboard
    constructor: (d) ->
      self = @

      byId = (id) -> ((w) -> w.id()==id)
      
      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []

      @campaignchartdaterange = new mod.DateRange
      @campaignchart = new mod.Chartdata

      @campaigntablescroller = new mod.Scroller
      @campaigntablesearchbar = new mod.Searchbar {
        availableCategories: ['Any','Paused','Finished','Active','Pending','Cancelled','Rejected']
        categoryTags:
          paused: 'Paused'
          finished: 'Finished'
          active: 'Active'
          pending: 'Pending'
          cancelled: 'Cancelled'
          rejected: 'Rejected'
        categoryFilter: 'state'
        searchFilter: 'name'
      }
      @campaigntable = new mod.Datatable(["name","state","revenue","cost","from","to"],
        state: (v) ->
          if 'paused' == v
            '<span class="label label-default"><span class="glyphicon glyphicon-pause"></span> Paused</span>'
          else if 'finished' == v
            '<span class="label label-primary"><span class="glyphicon glyphicon-flag"></span> Finished</span>'
          else if 'active' == v
            '<span class="label label-success"><span class="glyphicon glyphicon-play"></span> Active</span>'
          else if 'pending' == v
            '<span class="label label-info"><span class="glyphicon glyphicon-time"></span> Pending</span>'
          else if 'cancelled' == v
            '<span class="label label-warning"><span class="glyphicon glyphicon-ban-circle"></span> Cancelled</span>'
          else if 'rejected' == v
            '<span class="label label-danger"><span class="glyphicon glyphicon-warning-sign"></span> Rejected</span>'
          else
            v
        revenue: (v) -> '$'+v.toFixed(2)
        cost: (v) -> '$'+v.toFixed(2)
        from: (v) -> mod.datetostr(v)
        to: (v) -> mod.datetostr(v)
      )

      @confirmcampaigndelete = ko.observable(0)

      @publisher = ko.observable()
      @publishers = ko.observableArray []

      @audiences = ko.observableArray []
      @packages = ko.observableArray []
      @campaigns = ko.observableArray []

      @currentaudiences = ko.observableArray []
      @audienceposition = new mod.Counter {wrap:false,minValue:0}
      @currentpackages = ko.observableArray []
      @packageposition = new mod.Counter {wrap:false,minValue:0}

      @currentcampaign = ko.observable(new mod.Campaign {name:''})
      @campaignstep = new mod.Counter {value:1, minValue:1, maxValue:1, wrap:false}

      @currentpackage = ko.observable(new mod.Package {name:''})

      @results = ->
        ca = self.currentcampaign()
        pa = self.currentpackage()
        [r,s] = [0,0]
        if pa.id()<0 || ca.package()!=pa.id()
          id = ca.package()
          pa = p for p in self.packages() when id==p.id()
        if ca? && pa?
          #todo: model for audience
          c = pa.count()
          [r,s] = [(c*pa.salesCpm()/1000) || 0,(c*pa.buyCpm()/1000) || 0]
        {
          revenue: (0+r).toFixed(2)
          adspend: (0+s).toFixed(2)
          profit: (0+r-s).toFixed(2)
        }

      @pausecampaign = ->
        self.currentcampaign().state('paused')

      @activatecampaign = ->
        self.currentcampaign().state('active')

      @newpackage = ->
        ca = self.currentcampaign()
        self.currentpackage(new mod.Package {name:'Custom Package',id:0,campaign:ca.id()})

      @clearpackage = ->
        self.currentpackage(new mod.Package {name:'',id:-1})

      @checkmaxstep = ->
        ca = self.currentcampaign()
        m = 1
        if ca.package()? && ca.audiences().length>0
          m = 2
          if ca.creatives().length>0
            m = 3
        self.campaignstep.maxValue m

      @savepackage = ->
        a = self.currentpackage()
        ca = self.currentcampaign()
        alert('persist package')
        a.id Math.ceil 1000+1000*Math.random()
        ca.package a.id()
        self.packages.push a
        self.currentpackages.push a

      @newcampaign = ->
        self.confirmcampaigndelete 0
        self.currentcampaign(new mod.Campaign {name:'New Campaign',id:0,state:'pending'})
        self.currentpackage(new mod.Package {name:'',id:-1})
        (v.selected false; v.active false) for v in self.currentaudiences()
        (v.selected false; v.active false) for v in self.currentpackages()
        $('#editCampaign').modal 'show'
        ca = self.currentcampaign()
        self.currentaudiences (a.refreshSelf ca for a in self.audiences())
        self.currentpackages (p.refreshSelf ca for p in self.packages() when ca.id()==p.campaign() or not p.campaign()?)
        self.campaignstep.maxValue 1
        self.campaignstep.currentValue 1
        self.audienceposition.maxValue self.audiences().length
        self.audienceposition.currentValue ''
        self.packageposition.maxValue self.packages().length
        self.packageposition.currentValue ''
        self.checkmaxstep()

      @clearcampaign = ->
        self.currentcampaign(new mod.Campaign {name:'',id:-1})
        $('#editCampaign').modal 'hide'

      @cleardeletecampaign = ->
        self.confirmcampaigndelete 0

      @deletecampaign = ->
        if self.confirmcampaigndelete()==0
          return self.confirmcampaigndelete 1
        alert('delete campaign')
        self.currentcampaign(new mod.Campaign {name:'',id:-1})
        $('#editCampaign').modal 'hide'

      @savecampaign = ->
        a = self.currentcampaign()
        a.refresh(self.currentwebsites(),self.currentpackages())
        l = self.campaigntable.data
        if a.id() && a.id()>0
          alert('update campaign')
          l.remove byId a.id()
          l.push a
        else
          alert('persist campaign')
          a.id Math.ceil 1000+1000*Math.random()
          l.push a
        self.currentcampaign(new mod.Campaign {name:'',id:-1})
        $('#editCampaign').modal 'hide'

      @selectcampaign = (c) ->
        self.confirmcampaigndelete 0
        self.currentcampaign (new mod.Campaign()).copyFrom(c)
        self.currentpackage(new mod.Package {name:'',id:-1})
        (v.selected false; v.active false) for v in self.currentaudiences()
        (v.selected false; v.active false) for v in self.currentpackages()
        $('#editCampaign').modal 'show'
        ca = self.currentcampaign()
        self.currentaudiences (a.refreshSelf ca for a in self.audiences())
        self.currentpackages (p.refreshSelf ca for p in self.packages() when ca.id()==p.campaign() or not p.campaign()?)
        self.campaignstep.maxValue 1
        self.campaignstep.currentValue 1
        self.audienceposition.maxValue self.audiences().length
        self.audienceposition.currentValue ''
        self.packageposition.maxValue self.packages().length
        self.packageposition.currentValue ''
        self.checkmaxstep()
        
      @selectaudience = (c) ->
        if not c.active()
          v.active false for v in self.currentaudiences()
          c.active true
        else if c.selected()
          c.selected false
          self.currentcampaign().audiences.remove c.id()
        else
          c.selected true
          self.currentcampaign().audiences.push c.id()
        self.checkmaxstep()

      @selectpackage = (c) ->
        if not c.active()
          v.active false for v in self.currentpackages()
          c.active true
        else if not c.selected()
          v.selected false for v in self.currentpackages()
          c.selected true
          self.currentcampaign().package c.id()
        self.checkmaxstep()

      @addcreative = (c) ->
        alert('uploaded creative')

  models = new CampaignDashboard

  ko.applyBindings(models)

  models.campaigntable.rowClick = (c) ->
    models.selectcampaign c

  window.models = models
  #init

  models.publishers().map((p,i) ->
    pm = new mod.Publisher p
    models.publishers.push pm
    if p.active == "true"
      models.publisher pm
  )
  if !models.publisher() && models.publishers().length
    models.publisher models.publishers()[0]

  require(["lib/demodata"],(demo) ->
    demo.generate(mod,models,'campaign')
    models.campaignchartdaterange.dateRange 'Last Day'
    models.campaigntablesearchbar.filldata = ->
      models.campaigntable.data models.campaigntablesearchbar.filter models.campaigns()
    models.campaigntablesearchbar.datatable models.campaigntable
    models.campaigntablesearchbar.search()
  )
)
