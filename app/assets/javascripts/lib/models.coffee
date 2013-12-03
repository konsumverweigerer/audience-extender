define([ "knockout", "jsRoutes" ], (ko) ->
  day = 24*60*60*1000

  ranges = [ { name: 'Last Day', from: 2, to: 1, unit: 'day' },
     { name: 'This Week', from: 1, to: 0, unit: 'week' },
     { name: 'Last Week', from: 2, to: 1, unit: 'week' },
     { name: 'Last 2 Weeks', from: 3, to: 1, unit: 'week' }]

  truncateToDay = (d,s,e,u) ->
    base = new Date(d.getFullYear(),d.getMonth(),d.getDate())
    if u && u == 'week'
      dw = base.getDay()
      s = (7-dw)-(7*s)
      e = (7-dw)-(7*e)
      from = new Date base.getTime()+(s*day)
      to = new Date base.getTime()+(e*day)
    else
      from = new Date base.getTime()+((1-s)*day)
      to = new Date base.getTime()+((1-e)*day)
    [from,to]

  datetostr = (v) ->
    (v.getMonth()+1)+"/"+v.getDate()+"/"+v.getFullYear()

  strtodate = (s) ->
    v = s.split '/'
    new Date(v[2],v[0]-1,v[1])

  rangeNames = ->
    a = ['']
    for n in ranges
      a.push n.name 
    a

  class DateRange
    constructor: ->
      self = @

      @updating = false
      
      @availableDateRanges = ko.observableArray rangeNames()

      @startDateRaw = ko.observable truncateToDay(new Date(),2,1)[0]

      @startDate = ko.computed
        read: ->
          self.startDateRaw()
        write: (v) ->
          self.updating = true
          old = self.startDateRaw()
          self.startDateRaw v
          if old!=self.startDateRaw()
            self.loadData()
          self.updating = false
        owner: self

      @endDateRaw = ko.observable truncateToDay(new Date(),2,1)[1]

      @endDate = ko.computed
        read: ->
          self.endDateRaw()
        write: (v) ->
          self.updating = true
          old = self.endDateRaw()
          self.endDateRaw v
          if old!=self.endDateRaw()
            self.loadData()
          self.updating = false
        owner: self

      @format = ko.computed ->
        if self.endDate().getTime()-self.startDate().getTime() < 2*day
          return '%H:%M'
        return '%m/%d/%Y'

      @formattedStartDate = ko.computed
        read: ->
          datetostr self.startDate()
        write: (v) ->
          self.startDate strtodate v
        owner: self

      @formattedEndDate = ko.computed
        read: ->
          datetostr self.endDate() 
        write: (v) ->
          self.endDate strtodate v
        owner: self

      @dates = ko.computed
      	read: ->
      	  [self.startDate(),self.endDate()]
      	write: (v) ->
      	  if v.length == 2
      	    self.startDate v[0]
      	    self.endDate v[1]
      	owner: self
      	deferEvaluation: true

      @sameRange = (a,b) ->
        Math.abs(a[0].getTime()-b[0].getTime())<3600001 && Math.abs(a[1].getTime()-b[1].getTime())<3600001

      @dateRange = ko.computed
      	read: ->
      	  nd = new Date()
      	  cr = [self.startDate(),self.endDate()]
      	  nrs = (n.name for n in ranges when self.sameRange(cr,truncateToDay(nd,n.from,n.to,n.unit)))
      	  return nrs[0] || ''
      	write: (v) ->
          if not self.updating
            ranges.map (n,i)->
              if v == n.name
                t = truncateToDay(new Date(),n.from,n.to,n.unit)
                self.startDate t[0]
                self.endDate t[1]
#      	owner: self
#      	deferEvaluation: true

      @lastDay = ->
        t = truncateToDay(new Date(),2,1)
        self.startDate t[0]
        self.endDate t[1]

      @lastWeek = ->
        t = truncateToDay(new Date(),2,1,'week')
        self.startDate t[0]
        self.endDate t[1] 

      @loadData = -> self.dataloader()

      @dataloader = ->
        {}

  class Scroller
    constructor: ->
      self = @

      @fromIndex = ko.observable 1 

      @maxIndex = ko.observable 0 

      @shownPages = ko.observable 5

      @pageSize = ko.observable 10

      @currentPage = ko.computed -> Math.ceil(self.fromIndex() / self.pageSize())

      @toIndex = ko.computed -> Math.min(self.maxIndex(),self.fromIndex()+self.pageSize()-1)

      @maxPage = ko.computed -> Math.ceil(self.maxIndex() / self.pageSize())

      @hasData = ko.computed(-> self.maxIndex() > 0).extend({ throttle: 200 })

      @hasPages = ko.computed(-> self.maxPage() > 1).extend({ throttle: 200 })

      @hasNoPrev = ko.computed -> self.currentPage() < 2

      @hasNoNext = ko.computed -> self.currentPage() >= self.maxPage()

      @availablePages = ko.computed -> [0...self.maxPage()]

      @updating = ko.observable false

      @visiblePages = ko.computed ->
        i = 1
        mp = self.maxPage()
        sp = self.shownPages()
        cp = self.currentPage()
        i++ while (i+(sp/2)-1)<cp && (i+sp-1)<mp
        {page:i+j,active:(i+j)==cp} for j in [0...sp] when i+j<=mp

      @params = (fromIndex,maxIndex,pageSize)->
        if fromIndex?
          self.fromIndex fromIndex
        if maxIndex?
          self.maxIndex maxIndex
        if pageSize?
          self.pageSize pageSize

      @previous = ->
        c = self.fromIndex()
        ps = self.pageSize()
        if c > ps
          self.fromIndex c-ps

      @next = ->
        c = self.fromIndex()
        ps = self.pageSize()
        mp = self.maxIndex()
        if c+ps <= mp
          self.fromIndex c+ps

      @gotoPage = ->
        ps = self.pageSize()
        mp = self.maxIndex()
        ni = 1+((@page-1)*ps)
        if ni>0 && ni<=mp
          self.fromIndex ni

  class Searchbar
    constructor: (options)->
      self = @

      @availableCategories = ko.observableArray(options?.availableCategories || ['Status 1','Status 2','Status 3'])

      @categories = ko.observable(options?.categoryTags || {})

      @category = ko.observable options?.category

      @searchFilter = ko.observable options?.searchFilter

      @categoryFilter = ko.observable options?.categoryFilter

      @datatable = ko.observable()

      @hasData = ko.computed(->
        if self.datatable()?
          return self.datatable().data()?.length>0
        return true
      )

      @categoryTag = ko.computed ->
        (return tag) for tag,name of self.categories() when name==self.category()
        return

      @query = ko.observable ''

      @search = -> self.filldata()

      @filter = (d)->
        st = self.query()?.replace(/[^\w]/,'').split /\s+/
        sf = self.searchFilter()
        if st.length>0 && st[0]!='' && sf
          nd = []
          for r in d
            p = ko.unwrap(r[sf]).toLowerCase()
            for s in st when 0<=p.indexOf s.toLowerCase()
              nd.push r
              break
          d = nd
        ct = self.categoryTag()
        cf = self.categoryFilter()
        if ct && cf
          d = (r for r in d when (ko.unwrap r[cf])==ct)
        return d

      @filldata = ->
        {}

  class Chartdata
    constructor: ->
      self = @

      @chartcontentRaw = ko.observableArray []

      @chartcontent = ko.computed
        read: ->
          self.chartcontentRaw()
        write: (v) ->
          self.chartcontentRaw v
        owner: self

      @sums = ko.computed ->
        a = self.chartcontent() || []
        a.map (n,i) ->
          n.values.map((p,j) -> p.y).reduce (x,y) ->
            x+y
          , 0

      @calcsum = (cls) ->
        a = self.chartcontent() || []
        f = for n in a when cls==n.cls
            n.values.map((p) -> p.y).reduce((x,y) ->
              x+y
            , 0
            )
        f[0] || 0

      @sumadspend = ko.computed -> -(self.calcsum 'adspend').toFixed(0)

      @sumrevenue = ko.computed -> (self.calcsum 'revenue').toFixed(0)

      @sumprofit = ko.computed -> (self.calcsum 'profit').toFixed(0)

  class Datatable
    constructor: (headers, mapper) ->
      self = @

      @headers = ko.observableArray(headers || [])

      @data = ko.observableArray []

      @mapper = ko.observable(mapper || {})

      @resolve = (base,attr) ->
        if base.call
          self.resolve(base(),attr)
        else if attr
          if attr.split
            v = attr.split '.'
            r = v.splice 1
            self.resolve(base[v[0]],r.join '.')
          else
            base[attr]
        else
          base

      @rows = ko.computed ->
        h = self.headers()
        mm = self.mapper()
        self.data().map (n,i) ->
          h.map (m,j) ->
            dat = self.resolve(n,(n.indexOf? && h.indexOf m) || m)
            if mm?[m]
              mm[m] dat
            else
              dat

      @rowClick = (d) ->
        {}

  class Counter
    constructor: (options) ->
      options = options || {}
      self = @

      @currentValue = ko.observable(options.value ? 0)

      @minValue = ko.observable(options.minValue ? -Number.MAX_VALUE)

      @maxValue = ko.observable(options.maxValue ? Number.MAX_VALUE)

      @wrap = ko.observable(options.wrap || false)

      @isFirst = ko.computed -> self.currentValue()<=self.minValue()

      @isLast = ko.computed -> self.currentValue()>=self.maxValue()

      @isNotFirst = ko.computed -> self.currentValue()>self.minValue()

      @isNotLast = ko.computed -> self.currentValue()<self.maxValue()

      @previous = ->
        v = self.currentValue()
        if v==''
          v=0
        if v>self.minValue()
          self.currentValue v-1
        else if self.wrap()
          self.currentValue self.maxValue()

      @next = ->
        v = self.currentValue()
        if v==''
          v=0
        if v<self.maxValue()
          self.currentValue v+1
        else if self.wrap()
          self.currentValue self.minValue()

  class ServerModels
    typeOf: (name) ->
      if name=='real' || name=='persisted' || name=='transientnew'
        return { isIgnored: true }
      { isIgnored: false, isArray: false, isModel: false, model: null }

    constructor: (d) ->
      self = @

      @id = ko.observable d?.id

      @toJson = ->
        ko.toJSON(self)

      @copyFrom = (c) ->
        self.fromJson c.toMap()

      @toMap = ->
        m = {}
        for name, value of self
          v = self.toObject value
          if v?
            m[name] = v
        return m

      @toObject = (value) ->
        if value?.toMap
          value.toMap()
        else if ko.isObservable value
          self.toObject value()
        else if value instanceof Array and value?.indexOf
          self.toObject v for v in value
        else if value? and not value.call
          value
        else
          undefined

      @assign = (name,value) ->
        t = self.typeOf name
        if t.isIgnored
          return
        if ko.isObservable value
          value = value()
        if t.isModel
          if t.isArray
            value = (new t.model v for v in value)
          else
            value = new t.model value
        # todo: handle models
        if self[name]
          if ko.isObservable self[name]
            self[name] value
          else
            self[name] = value

      @fromJson = (json) ->
        self.assign(n,v) for n, v of json
        return self

      @real = ko.computed -> (self.id() ? -1)>=0

      @persisted = ko.computed -> (self.id() ? -1)>0

      @transientnew = ko.computed -> (self.id() ? -1)==0

      @saveApply = (r) ->
        if r.messages? && r.messages.length!=0
          for v in r.messages
            self.messages.push new Message v
        self.fromJson r.data

      @saveApply = (r) ->
        if r.messages? && r.messages.length!=0
          for v in r.messages
            self.messages.push new Message v
        self.fromJson r.data

      @save = (page, success) ->
        route = saveRoute()
        if route?
          if page.loader?
            page.loader.next()
          result = route.ajax
            data: self.toMap
            success: (r) ->
              if page.loader?
                page.loader.previous()
              saveApply r
              if success? && r.messages? && r.messages.length==0
                success r
            fail: (r) ->
              if page.loader?
                page.loader.previous()
              if page.alert?
                page.alert.show
                  title: 'Server not available'
                  content: 'Action could not be performed'
                  priority: 'error'

      @remove = (page) ->
        route = removeRoute()
        if route?
          if page.loader?
            page.loader.next()
          result = route.ajax
            data: self.toMap
            success: (r) ->
              if page.loader?
                page.loader.previous()
              removeApply r
              if success? && r.messages? && r.messages.length==0
                success r
            fail: (r) ->
              if page.loader?
                page.loader.previous()
              if page.alert?
                page.alert.show
                  title: 'Server not available'
                  content: 'Action could not be performed'
                  priority: 'error'

      @saveRoute = (page) ->

      @removeRoute = (page) ->

  class Message extends ServerModels
    typeOf: (name) ->
      if name=='name' || name=='dismissed'
        return { isIgnored: true }
      super(name)

    constructor: (dortitle,content,priority) ->
      super()
      if dortitle instanceof Object
        title = dortitle.title
        content = dortitle.content
        priority = dortitle.priority
      else
        title = dortitle
      self = @

      @name = ko.observable 'alert'

      @title = ko.observable title

      @content = ko.observable content

      @dismissed = ko.observable 0

      @priority = ko.observable(priority || 'info')

      @isInfo = ko.computed -> self.priority()=='info'

      @isWarning = ko.computed -> self.priority()=='warning'

      @isError = ko.computed -> self.priority()=='error'

      @show = (title,content,priority) ->
        if title?
          self.title title
        if content?
          self.content content
        if priority?
          self.priority priority
        $('#'+self.name()).modal('show')
        self.dismissed -1

      @hide = ->
        $('#'+self.name()).modal('hide')
        self.dismissed 0

      @dismiss = ->
        if self.dismissed()==-1
          self.hide()
        else
          self.dismissed 1

  class Campaign extends ServerModels
    typeOf: (name) ->
      if name=='messages'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @state = ko.observable d?.state

      @revenue = ko.observable d?.revenue

      @cost = ko.observable d?.cost

      @package = ko.observable d?.package

      @audiences = ko.observableArray d?.audiences

      @creatives = ko.observableArray d?.creatives

      @from = ko.observable d?.from

      @to = ko.observable d?.to

      @refresh = (audiences,packages) ->
        for au in audiences
          au.selected false
          for a in self.audiences()
            if a==au.id()
              au.selected true
        pak = self.package()
        for pa in packages
          pa.selected false
          if pak && pak.id()==pa.id()
            pa.selected true
        return self

  class PathTarget extends ServerModels
    constructor: (d) ->
      super(d)
      self = @

      @path = ko.observable d?.path

      @website = ko.observable d?.website

      @active = ko.observable d?.active

  class Audience extends ServerModels
    typeOf: (name) ->
      if name=='paths'
        return { isIgnored: false, isArray: true, isModel: true, model: PathTarget }
      else if name=='currentpaths' || name=='activewebsite' || name=='currentallpath' || name=='path' || name=='nonempty' || name=='messages' || name=='selected' || name=='active'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @state = ko.observable d?.state

      @tracking = ko.observable d?.tracking

      @count = ko.observable d?.count

      @activewebsite = ko.observable d?.activewebsite

      @websites = ko.observableArray(d?.websites || [])

      @selected = ko.observable false

      @active = ko.observable false

      @nonempty = ko.computed -> (self.websites() || []).length>0

      @websiteNames = ko.observable ''

      @websiteNamesShort = ko.observable ''

      @path = ko.observable()

      @paths = ko.observableArray (d?.paths || []).map (v) -> new PathTarget v

      @allpaths = ko.observable(d?.allpaths || {})

      @currentallpath = ko.computed
        read: ->
          aw = self.activewebsite()
          ( aw && self.allpaths()[aw] ) || 'off'
        write: (v) ->
          aw = self.activewebsite()
          if aw
            w = self.allpaths()
            w[aw] = v
            self.allpaths w
        owner: self.allpaths

      @currentpaths = ko.computed ->
        self.paths().filter (n,i) ->
          w = n.website()
          aw = self.activewebsite()
          w && aw && w==aw

      @addpath = ->
        if self.path()? && self.path()!=''
          for p in self.currentpaths()
            if p.path()==self.path()
              self.messages.push new Message
                title: 'Duplicate path'
                content: 'Path already present in website'
                priority: 'warning'
              return
          self.paths.push new PathTarget
            path: self.path()
            website: self.activewebsite()
            active: not self.currentallpath()
          self.path ''

      @checkpath = (v)->
        if v? && v!=''
          for p in self.currentpaths()
            if p.path()==v
              return [false,'Path already present in website']
        return [true]

      @removepath = (path) ->
        self.paths.remove (v) ->
          v.website()==path.website() && v.path()==path.path()

      @refreshSelf = (campaign) ->
        id = self.id()
        if not (self.selected true for au in campaign.audiences() when au==id).length
          self.selected false
        return self

      @refresh = (websites) ->
        n = []
        for web in websites
          web.selected false
          for wi in self.websites()
            if wi==web.id()
              n.push web.name()
              web.selected true
        n = n.join(', ')
        self.websiteNames n
        if n.length > 20
          n = n.substring(0,20)+' ...'
        self.websiteNamesShort n
        return self

  class Website extends ServerModels
    typeOf: (name) ->
      if name=='active' || name=='inactive' || name=='editing' || name=='selected' || name=='emailSent' || name=='emailFail' || name=='emailStatus' || name=='messages'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @code = ko.observable d?.code

      @count = ko.observable d?.count

      @email = ko.observable d?.email

      @active = ko.observable false

      @inactive = ko.computed -> not self.active() 

      @editing = ko.observable false

      @selected = ko.observable false

      @refreshSelf = (audience) ->
        id = self.id()
        if not (self.selected true for wi in audience.websites() when wi==id).length
          self.selected false
        return self

      @emailStatus = ko.observable ''

      @emailSent = ko.computed -> self.emailStatus()=='success'

      @emailFail = ko.computed -> self.emailStatus()=='fail'

      @sendemail = ->
        if self.sendcodebyemail(self.id(),self.email())
          self.emailStatus 'success'
        else
          self.emailStatus 'fail'

      @sendcodebyemail = (id,email)->
        Math.floor 2*Math.random()

  class Package extends ServerModels
    typeOf: (name) ->
      if name=='messages' || name=='selected' || name=='active'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @selected = ko.observable false

      @active = ko.observable false

      @campaign = ko.observable d?.campaign

      @refreshSelf = (campaign) ->
        self.selected campaign.package()==self.id()
        return self

      @startDate = ko.observable(d?.startDate ? new Date())

      @endDate = ko.observable(d?.endDate ? new Date())

      @dates = ko.computed
        read: ->
          [self.startDate(),self.endDate()]
        write: (v) ->
          if v.length == 2
            self.startDate v[0]
            self.endDate v[1]

      @count = ko.observable(d?.count).extend { integers: 10 }

      @reach = ko.observable d?.reach

      @goal = ko.observable d?.goal

      @buyCpm = ko.observable(d?.buyCpm)

      @salesCpm = ko.observable(d?.salesCpm)

  class Creative extends ServerModels
    constructor: (d) ->
      super(d)
      self = @

      @name = ko.observable d?.name

      @url = ko.observable d?.url

      @previewUrl = ko.observable d?.previewUrl

      @data = ko.observable d?.data

  class Publisher extends ServerModels
    typeOf: (name) ->
      if name=='messages'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @active = ko.observable d?.active

      @admins = ko.observableArray (d?.admins || []).map (v) -> new Admin v

  class Admin extends ServerModels
    typeOf: (name) ->
      if name=='messages'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @email = ko.observable d?.email

      @roles = ko.observableArray d?.roles

      @publishers = ko.observableArray (d?.publishers || []).map (v) -> new Publisher v

  { Message: Message,
  Datatable: Datatable,
  Chartdata: Chartdata,
  Searchbar: Searchbar,
  Scroller: Scroller,
  DateRange: DateRange,
  Counter: Counter,
  Campaign: Campaign,
  Audience: Audience,
  Website: Website,
  Admin: Admin,
  Package: Package,
  Creative: Creative,
  PathTarget: PathTarget,
  Publisher: Publisher,
  truncateToDay: truncateToDay,
  datetostr: datetostr,
  strtodate: strtodate }
)
