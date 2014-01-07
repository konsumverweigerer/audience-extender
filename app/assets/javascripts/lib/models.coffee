define(['knockout', 'jsRoutes'], (ko) ->
  day = 24*60*60*1000

  ranges = [ { name: 'Last Day', from: 2, to: 1, unit: 'day' },
     { name: 'This Week', from: 1, to: 0, unit: 'week' },
     { name: 'Last Week', from: 2, to: 1, unit: 'week' },
     { name: 'Last 2 Weeks', from: 3, to: 1, unit: 'week' }]

  rangedays = (from,to) ->
    ft = from.getTime()
    tt = to.getTime()
    if ft<tt
      t for t in [ft..tt] by day
    else
      [0]

  dayrange = (d,s,e) ->
    base = new Date(d.getFullYear(),d.getMonth(),d.getDate())
    from = (new Date base.getTime()+((1-s)*day)).getTime()
    to = (new Date base.getTime()+((e)*day)).getTime()
    t for t in [from..to] by day

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
    return [from,to]

  datetostr = (v) ->
    if v instanceof Date
      return (v.getMonth()+1)+"/"+v.getDate()+"/"+v.getFullYear()
    return ''

  strtodate = (s) ->
    v = s.split '/'
    new Date(v[2],v[0]-1,v[1])

  rangeNames = ->
    a = ['']
    for n in ranges
      a.push n.name
    return a

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

      @hasData = ko.computed(-> self.maxIndex() > 0).extend
        throttle: 200

      @hasPages = ko.computed(-> self.maxPage() > 1).extend
        throttle: 200

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
        if base?.call
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

      @isInner = ko.computed ->
        self.currentValue()>self.minValue() && self.currentValue()<self.maxValue()

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
      if ['real','persisted','transientnew'].indexOf(name)>=0
        return { isIgnored: true, isSend: false }
      { isIgnored: false, isSend: true, isArray: false, isModel: false, model: null }

    copyFrom: (c) ->
      @fromJson c.toMap()

    toJson: ->
      ko.toJSON(self)

    constructor: (d) ->
      self = @

      @id = ko.observable d?.id

      @toMap = ->
        m = {}
        for name, value of self
          v = self.toObject value
          if v?
            m[name] = v
        return m

      @toSendMap = ->
        m = {}
        for name, value of self
          t = self.typeOf name
          if t?.isSend?
            v = self.toObject value
            if v?
              m[name] = v
        return m

      @toObject = (value) ->
        if value?.toMap
          value.toMap()
        else if ko.isObservable value
          self.toObject value()
        else if value instanceof Array && value?.indexOf?
          self.toObject v for v in value
        else if value instanceof Object && value?.read?
          value.read()
        else if value? && not value.call
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

      @removeApply = (r) ->
        if r.messages? && r.messages.length!=0
          for v in r.messages
            self.messages.push new Message v
        self.fromJson r.data

      @save = (page, success) ->
        route = self.saveRoute page
        if route?
          if page.loader?
            page.loader.next()
          result = route.ajax
            data: $.param(self.toSendMap()).replace(/[%]5B([A-Za-z]*)[%]5D=/g,'.$1=')
            success: (r) ->
              if page.loader?
                page.loader.previous()
              self.saveApply r
              if success? && r.messages? && r.messages.length==0
                success r
            error: (r) ->
              if page.loader?
                page.loader.previous()
              if page.alert?
                page.alert.show
                  title: 'Server not available'
                  content: 'Action could not be performed'
                  priority: 'error'

      @remove = (page) ->
        route = self.removeRoute page
        if route?
          if page.loader?
            page.loader.next()
          result = route.ajax
            data: self.toMap()
            success: (r) ->
              if page.loader?
                page.loader.previous()
              self.removeApply r
              if success? && r.messages? && r.messages.length==0
                success r
            error: (r) ->
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
      if ['name','dismissed'].indexOf(name)>=0
        return { isIgnored: true, isSend: false }
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

      @actions = ko.observableArray []

      @dismissed = ko.observable 0

      @priority = ko.observable(priority || 'info')

      @isInfo = ko.computed -> self.priority()=='info'

      @isWarning = ko.computed -> self.priority()=='warning'

      @isError = ko.computed -> self.priority()=='error'

      @isSuccess = ko.computed -> self.priority()=='success'

      @show = (dortitle,content,priority) ->
        if dortitle instanceof Object
          title = dortitle.title
          content = dortitle.content
          priority = dortitle.priority
        else
          title = dortitle
        if title?
          self.title title
        if content?
          self.content content
        if priority?
          self.priority priority
        $('#'+self.name()).modal 'show'
        self.dismissed -1

      @hide = ->
        $('#'+self.name()).modal 'hide'
        self.dismissed 0

      @dismiss = ->
        if self.dismissed()==-1
          self.hide()
        else
          self.dismissed 1

  class Creative extends ServerModels
    constructor: (d) ->
      super d
      self = @

      @name = ko.observable d?.name

      @url = ko.observable d?.url

      @state = ko.observable d?.state

      @variant = ko.observable d?.variant

      @previewUrl = ko.observable d?.previewUrl

      @uuid = ko.observable d?.uuid

      @data = ko.observable d?.data

      @saveRoute = (page) ->
        routes.controllers.AdminController.creativeSave()

  class Campaign extends ServerModels
    typeOf: (name) ->
      if ['messages','uploadprogress','selected','schedulechart'].indexOf(name)>=0
        return { isIgnored: true, isSend: false }
      super name

    copyFrom: (c) ->
      ca = super c
      ca.dataloader = c.dataloader
      return ca

    constructor: (d) ->
      super d
      self = @

      @messages = ko.observableArray []

      @uploadprogress = new Counter {wrap:false,minValue:0,maxValue:100}

      @selected = ko.observable false

      @name = ko.observable d?.name

      @state = ko.observable(d?.state || 'P')

      @variant = ko.observable d?.variant

      @revenue = ko.observable(d?.revenue || 0)

      @cost = ko.observable(d?.cost || 0)

      @package = ko.observable d?.package

      @audiences = ko.observableArray(d?.audiences || [])

      @creatives = ko.observableArray(new Creative x for x in (d?.creatives || []))

      @startDate = ko.observable().extend
        datetime: 'full'
      @startDate(d?.startDate)

      @endDate = ko.observable().extend
        datetime: 'full'
      @endDate(d?.startDate)

      @schedulechart = new Chartdata

      @dates = ko.computed(
        read: ->
          [self.startDate(),self.endDate()]
        write: (v) ->
          if v.length == 2
            self.startDate v[0]
            self.endDate v[1]
      ).extend
        throttle: 100

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

      @failedupload = (e,data) ->
        if e?.type=='fileuploadprocessfail'
          for f in data.files when f.error?
            self.messages.push new Message
              title: 'Could not upload file'
              content: f.error
              priority: 'error'

      @addupload = (e,data) -> {}

      @refreshdata = ->
        self.dataloader self
      @dates.subscribe ->
        self.refreshdata()

      @dataloader = (ca) -> {}

      @saveRoute = (page) ->
        routes.controllers.CampaignController.campaignSave(page.publisher().id())

  class PathTarget extends ServerModels
    constructor: (d) ->
      super d
      self = @

      @path = ko.observable d?.path

      @website = ko.observable d?.website

      @website = ko.computed ->
        v = self.website()
        if v?.id
          return ko.unwrap v.id
        return v

      @active = ko.observable d?.active

      @include = ko.computed -> self.active()=='on'

  class Audience extends ServerModels
    typeOf: (name) ->
      if name=='paths'
        return { isIgnored: false, isSend: true, isArray: true, isModel: true, model: PathTarget }
      else if ['websitePaths'].indexOf(name)>=0
        return { isIgnored: true, isSend: true }
      else if ['currentpaths','activewebsite','currentallpath','path','nonempty','messages','selected','active'].indexOf(name)>=0
        return { isIgnored: true, isSend: false }
      super(name)

    constructor: (d) ->
      super d
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

      @startDate = ko.observable().extend
        datetime: 'full'
      @startDate(d?.startDate)

      @endDate = ko.observable().extend
        datetime: 'full'
      @endDate(d?.startDate)

      @dates = ko.computed
        read: ->
          [self.startDate(),self.endDate()]
        write: (v) ->
          if v.length == 2
            self.startDate v[0]
            self.endDate v[1]

      @nonempty = ko.computed -> (self.websites() || []).length>0

      @websiteNames = ko.observable ''

      @websiteNamesShort = ko.observable ''

      @path = ko.observable()

      @paths = ko.observableArray (d?.paths || []).map (v) -> new PathTarget v

      @allpathsbyid = (id, c) ->
        if c?
          self.allpaths()[id] = c
        else
          self.allpaths()[id]

      @allpaths = ko.observable(d?.allpaths || {})

      @currentallpath = ko.computed
        read: ->
          aw = self.activewebsite()
          ( aw && self.allpaths()[aw] ) || 'off'
        write: (v) ->
          aw = self.activewebsite()
          if aw
            n = $.extend({},self.allpaths())
            n[aw] = v
            self.allpaths(n)

      @websitePaths =
        read: ->
          for we in self.websites()
            s = self.allpathsbyid we.id
            {id: we.id, name: we.name,allPath: (s!='off')}

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
        if not (self.selected true for au in campaign.audiences() when au?.id==id).length
          self.selected false
        return self

      @refresh = (websites) ->
        n = []
        ids = []
        for wi in self.websites()
          ids.push wi.id
          if wi.name?
            n.push wi.name
          else
            n.push web.name() for web in websites when wi?.id==web.id()
        for web in websites
          web.selected ids.indexOf(web.id())>=0
        n = n.join(', ')
        self.websiteNames n
        if n.length > 20
          n = n.substring(0,20)+' ...'
        self.websiteNamesShort n
        return self

      @saveRoute = (page) ->
        routes.controllers.AudienceController.audienceSave(page.publisher().id())

  class Website extends ServerModels
    typeOf: (name) ->
      if ['active','inactive','editing','selected','emailSent','emailFail','emailStatus','messages','codeCopied'].indexOf(name)>=0
        return { isIgnored: true, isSend: false }
      super(name)

    constructor: (d) ->
      super d
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @url = ko.observable d?.url

      @code = ko.observable d?.code

      @codeCopied = ko.observable()

      @count = ko.observable (d?.count || 0)

      @email = ko.observable d?.email

      @active = ko.observable false

      @inactive = ko.computed -> not self.active()

      @editing = ko.observable false

      @selected = ko.observable false

      @refreshSelf = (audience) ->
        id = self.id()
        if not (self.selected true for wi in audience.websites() when wi?.id==id).length
          self.selected false
        return self

      @emailStatus = ko.observable ''

      @emailSent = ko.computed -> self.emailStatus()=='success'

      @emailFail = ko.computed -> self.emailStatus()=='fail'

      @sendemail = -> true

      @sendcodebyemail = (id,email,page)->
        routes.controllers.AudienceController.sendWebsiteCode(email,page.publisher().id(),id).ajax
          success: (r) ->
            s = 'success'
            for m in r.messages
              message = new Message m
              self.messages.push message
              if message.isError()
                s = 'error'
            if r.messages.length==0
              self.messages.push new Message
                title: 'E-mail sent successfully to '+email
                content: 'Check your e-mail for code'
                priority: 'success'
            self.emailStatus s
          error: (r) ->
            self.messages.push new Message
              title: 'Server error'
              content: 'Could not send e-mail to '+email
              priority: 'error'
            self.emailStatus 'fail'

      @saveRoute = (page) ->
        routes.controllers.AudienceController.websiteSave(page.publisher().id())

  class Package extends ServerModels
    typeOf: (name) ->
      if ['messages','selected','active'].indexOf(name)>=0
        return { isIgnored: true, isSend: false }
      super(name)

    constructor: (d) ->
      super d
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @selected = ko.observable false

      @active = ko.observable false

      @campaign = ko.observable d?.campaign

      @refreshSelf = (campaign) ->
        self.selected campaign.package()?.id==self.id()
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

      @saveRoute = (page) ->
        routes.controllers.CampaignController.packageSave(page.publisher().id())

  class Cookie extends ServerModels
    constructor: (d) ->
      super d
      self = @

      @name = ko.observable d?.name

      @content = ko.observable d?.content

      @state = ko.observable d?.state

      @variant = ko.observable d?.variant

      @uuid = ko.observable d?.uuid

      @data = ko.observable d?.data

      @saveRoute = (page) ->
        routes.controllers.AdminController.cookieSave()

  class Publisher extends ServerModels
    typeOf: (name) ->
      if name=='messages'
        return { isIgnored: true, isSend: false }
      super(name)

    constructor: (d) ->
      super d
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @active = ko.observable d?.active

      @url = ko.observable d?.url

      @streetaddress1 = ko.observable d?.streetaddress1

      @streetaddress2 = ko.observable d?.streetaddress2

      @streetaddress3 = ko.observable d?.streetaddress3

      @state = ko.observable d?.state

      @country = ko.observable d?.country

      @telephone = ko.observable d?.telephone

      @admins = ko.observableArray (d?.admins || []).map (v) -> new Admin v

      @saveRoute = (page) ->
        routes.controllers.PublisherController.publisherSave()

  class Admin extends ServerModels
    typeOf: (name) ->
      if name=='messages'
        return { isIgnored: true, isSend: false }
      super(name)

    constructor: (d) ->
      super d
      self = @

      @messages = ko.observableArray []

      @name = ko.observable d?.name

      @email = ko.observable d?.email

      @password = ko.observable ''

      @verify = ko.observable ''

      @url = ko.observable d?.url

      @streetaddress1 = ko.observable d?.streetaddress1

      @streetaddress2 = ko.observable d?.streetaddress2

      @streetaddress3 = ko.observable d?.streetaddress3

      @state = ko.observable d?.state

      @country = ko.observable d?.country

      @telephone = ko.observable d?.telephone

      @roles = ko.observableArray d?.roles

      @publishers = ko.observableArray (d?.publishers || []).map (v) -> new Publisher v

      @saveRoute = (page) ->
        routes.controllers.AdminController.adminSave()

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
  Cookie: Cookie,
  PathTarget: PathTarget,
  Publisher: Publisher,
  truncateToDay: truncateToDay,
  dayrange: dayrange,
  rangedays: rangedays,
  datetostr: datetostr,
  strtodate: strtodate }
)
