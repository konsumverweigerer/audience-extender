define(["webjars!knockout.js"], (ko) ->
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
      from = new Date(base.getTime()+(s*day))
      to = new Date(base.getTime()+(e*day))
    else
      from = new Date(base.getTime()+((1-s)*day))
      to = new Date(base.getTime()+((1-e)*day))
    [from,to]

  datetostr = (v) ->
    (v.getMonth()+1)+"/"+v.getDate()+"/"+v.getFullYear()

  strtodate = (s) ->
    v = s.split('/')
    new Date(v[2],v[0]-1,v[1])

  rangeNames = ->
    n.name for n in ranges

  class DateRange
    constructor: ->
      self = @

      @availableDateRanges = ko.observableArray rangeNames()

      @startDateRaw = ko.observable truncateToDay(new Date(),2,1)[0]

      @startDate = ko.computed
        read: ->
          self.startDateRaw()
        write: (v) ->
          old = self.startDateRaw()
          self.startDateRaw v
          if old!=self.startDateRaw()
            self.loadData()
        owner: self

      @endDateRaw = ko.observable truncateToDay(new Date(),2,1)[1]

      @endDate = ko.computed
        read: ->
          self.endDateRaw()
        write: (v) ->
          old = self.endDateRaw()
          self.endDateRaw v
          if old!=self.endDateRaw()
            self.loadData()
        owner: self

      @format = ko.computed( ->
        if self.endDate().getTime()-self.startDate().getTime() < 2*day
          return '%H:%M'
        return '%m/%d/%Y'
      )

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
      	    self.startDate(v[0])
      	    self.endDate(v[1])
      	owner: self
      	deferEvaluation: true

      @sameRange = (a,b) ->
        Math.abs(a[0].getTime()-b[0].getTime())<3600001 && Math.abs(a[1].getTime()-b[1].getTime())<3600001

      @dateRange = ko.computed
      	read: ->
      	  nd = new Date()
      	  cr = [ self.startDate(), self.endDate() ]
      	  nrs = (n.name for n in ranges when self.sameRange(cr,truncateToDay(nd,n.from,n.to,n.unit)))
      	  return nrs[0] || ''
      	write: (v) ->
          ranges.map((n,i)->
            if v == n.name
              t = truncateToDay(new Date(),n.from,n.to,n.unit)
              self.startDate(t[0])
              self.endDate(t[1])
          )
      	owner: self
      	deferEvaluation: true

      @lastDay = ->
        t = truncateToDay(new Date(),2,1)
        self.startDate t[0]
        self.endDate t[1]

      @lastWeek = ->
        t = truncateToDay(new Date(),2,1,'week')
        self.startDate t[0]
        self.endDate t[1] 

      @loadData = ->
        self.dataloader()

      @dataloader = ->
        {}

  class Scroller
    constructor: ->
      self = @

      @fromIndex = ko.observable 1 

      @maxIndex = ko.observable 0 

      @shownPages = ko.observable 5

      @pageSize = ko.observable 10

      @currentPage = ko.computed( ->
        Math.ceil(self.fromIndex() / self.pageSize())
      )

      @toIndex = ko.computed( ->
        Math.min(self.maxIndex(),self.fromIndex()+self.pageSize()-1)
      )

      @maxPage = ko.computed( ->
        Math.ceil(self.maxIndex() / self.pageSize())
      )

      @hasData = ko.computed( ->
        self.maxIndex() > 0
      )

      @hasNoPrev = ko.computed( ->
        self.currentPage() < 2
      )

      @hasNoNext = ko.computed( ->
        self.currentPage() >= self.maxPage()
      )

      @availablePages = ko.computed( ->
        [0...self.maxPage()]
      )

      @visiblePages = ko.computed( ->
        i = 1
        mp = self.maxPage()
        sp = self.shownPages()
        cp = self.currentPage()
        while (i+(sp/2)-1)<cp && (i+sp-1)<mp
          i++
        {page:i+j,active:(i+j)==cp} for j in [0...sp] when i+j<=mp
      )

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
    constructor: ->
      self = @

      @availableCategories = ko.observableArray ['Status 1','Status 2','Status 3']

      @category = ko.observable ''

      @query = ko.observable ''

      @search = ->
        self.filldata()

      @filldata = ->
        {}

  class Chartdata
    constructor: ->
      self = @

      @chartcontentRaw = ko.observableArray [
        {key: 'Revenue', values: [{x:0,y:5000}]},
        {key: 'Ad spend', values: [{x:0,y:4000}]},
        {key: 'Profit', values: [{x:0,y:3000}]}]

      @chartcontent = ko.computed(
        read: ->
          self.chartcontentRaw()
        write: (v) ->
          self.chartcontentRaw v
        owner: self
      )

      @sums = ko.computed( ->
        a = self.chartcontent() || []
        a.map((n,i) ->
          n.values.map((p,j) ->
            p.y
          ).reduce((x,y) ->
            x+y
          , 0
          )
        )
      )

      @calcsum = (cls) ->
        a = self.chartcontent() || []
        f = a.filter((n,i) ->
            cls == n.cls
          ).map((n,i) ->
            n.values.map((p,j) ->
              p.y
            ).reduce((x,y) ->
              x+y
            , 0
            )
          )
        f[0] || 0

      @sumadspend = ko.computed( ->
        -self.calcsum('adspend').toFixed(0)
      )

      @sumrevenue = ko.computed( ->
        self.calcsum('revenue').toFixed(0)
      )

      @sumprofit = ko.computed( ->
        self.calcsum('profit').toFixed(0)
      )

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
            v = attr.split('.')
            r = v.splice(1)
            self.resolve(base[v[0]],r.join('.'))
          else
            base[attr]
        else
          base

      @rows = ko.computed( ->
        h = self.headers()
        mm = self.mapper()
        self.data().map( (n,i) ->
          h.map( (m,j) ->
            dat = self.resolve(n,(n.indexOf && h.indexOf(m)) || m)
            if mm && mm[m]
              mm[m](dat)
            else
              dat
          )
        )
      )

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

      @isFirst = ko.computed( ->
        v = self.currentValue()
        if v==''
          v=0
        v<=self.minValue()
      )

      @isLast = ko.computed( ->
        v = self.currentValue()
        if v==''
          v=0
        v>=self.maxValue()
      )

      @isNotFirst = ko.computed( ->
        v = self.currentValue()
        if v==''
          v=0
        v>self.minValue()
      )

      @isNotLast = ko.computed( ->
        v = self.currentValue()
        if v==''
          v=0
        v<self.maxValue()
      )

      @previous = ->
        v = self.currentValue()
        if v==''
          v=0
        if v>self.minValue()
          self.currentValue(v-1)
        else if self.wrap()
          self.currentValue(self.maxValue())

      @next = ->
        v = self.currentValue()
        if v==''
          v=0
        if v<self.maxValue()
          self.currentValue(v+1)
        else if self.wrap()
          self.currentValue(self.minValue())

  class ServerModels
    typeOf: (name) ->
      if name=='real' || name=='persisted'
        return { isIgnored: true }
      { isIgnored: false, isArray: false, isModel: false, model: null }

    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @toJson = ->
        ko.toJSON(self)

      @copyFrom = (c) ->
        self.fromJson(c.toMap())

      @toMap = ->
        m = {}
        for name, value of self
          v = self.toObject(value)
          if v?
            m[name] = v
        return m

      @toObject = (value) ->
        if value?.toMap
          value.toMap()
        else if ko.isObservable value
          self.toObject(value())
        else if value instanceof Array and value?.indexOf
          self.toObject(v) for v in value
        else if value? and not value.call
          value
        else
          undefined

      @assign = (name, value) ->
        t = self.typeOf(name)
        if t.isIgnored
          return
        if ko.isObservable value 
          value = value()
        if t.isModel
          if t.isArray
            value = (new t.model(v) for v in value)
          else
            value = new t.model(value)
        # todo: handle models
        if self[name]
          if ko.isObservable self[name]
            self[name] value 
          else
            self[name] = value

      @fromJson = (json) ->
        self.assign(n,v) for n, v of json
        return self

      @real = ko.computed( ->
          id = self.id()
          id? && id>=0
      )

      @persisted = ko.computed( ->
          id = self.id()
          id? && id>0
      )

  class Message extends ServerModels
    constructor: (dortitle,content,priority) ->
      super()
      if dortitle instanceof Object
        title = dortitle.title
        content = dortitle.content
        priority = dortitle.priority
      else
        title = dortitle
      self = @

      @title = ko.observable(title)

      @content = ko.observable(content)

      @priority = ko.observable(priority)

      @dismiss = () ->
        {}

  class Campaign extends ServerModels
    constructor: (d) ->
      super(d)
      self = @

      @name = ko.observable(d && d.name)

      @state = ko.observable(d && d.state)

      @revenue = ko.observable(d && d.revenue)

      @cost = ko.observable(d && d.cost)

      @from = ko.observable(d && d.from)

      @to = ko.observable(d && d.to)

  class PathTarget extends ServerModels
    constructor: (d) ->
      super(d)
      self = @

      @path = ko.observable(d && d.path)

      @website = ko.observable(d && d.website)

      @active = ko.observable(d && d.active)

  class Audience extends ServerModels
    typeOf: (name) ->
      if name=='paths'
        return { isIgnored: false, isArray: true, isModel: true, model: PathTarget }
      else if name=='currentpaths' || name=='activewebsite' || name=='currentallpath' || name=='path' || name=='nonempty'
        return { isIgnored: true }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @name = ko.observable(d && d.name)

      @state = ko.observable(d && d.state)

      @tracking = ko.observable(d && d.tracking)

      @count = ko.observable(d && d.count)

      @activewebsite = ko.observable(d && d.activewebsite)

      @websites = ko.observableArray(d && d.websites)

      @nonempty = ko.computed( ->
        (self.websites() || []).length>0
      )

      @websiteNames = ko.observable('')

      @path = ko.observable()

      @paths = ko.observableArray(((d && d.paths) || []).map((v) ->
        new PathTarget v
      ))
      
      @allpaths = ko.observable((d && d.allpaths) || {})
      
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

      @currentpaths = ko.computed( ->
        self.paths().filter((n,i) ->
          w = n.website()
          aw = self.activewebsite()
          w && aw && w==aw
        )
      )

      @addpath = ->
        self.paths.push(new PathTarget
          path: self.path()
          website: self.activewebsite()
          active: not self.currentallpath()
        )

      @removepath = (path) ->
        self.paths.remove((v) ->
          v.website()==path.website() && v.path()==path.path()
        )

      @refresh = (websites) ->
        n = []
        for wi in self.websites()
          for web in websites
            if wi==web.id()
              n.push web.name()
              break
        n = n.join(', ')
        if n.length > 20
          n = n.substring(0,20)+' ...'
        self.websiteNames(n)
        return self

  class Website extends ServerModels
    typeOf: (name) ->
      if name=='active' || name=='editing' || name=='selected'
        return { isIgnored: true, isArray: false, isModel: false, model: null }
      super(name)

    constructor: (d) ->
      super(d)
      self = @

      @name = ko.observable(d && d.name)

      @code = ko.observable(d && d.code)

      @count = ko.observable(d && d.count)

      @email = ko.observable(d && d.email)

      @active = ko.observable false

      @inactive = ko.computed( ->
        not self.active()
      )

      @editing = ko.observable false

      @selected = ko.observable false

      @refresh = (audience) ->
        self.selected(wi==self.id()) for wi in audience.websites()
        return self

      @emailStatus = ko.observable ''

      @emailSent = ko.computed( ->
        self.emailStatus()=='success'
      )

      @emailFail = ko.computed( ->
        self.emailStatus()=='fail'
      )

      @sendemail = ->
        if self.sendcodebyemail(self.id(),self.email())
          self.emailStatus('success')
        else
          self.emailStatus('fail')

      @sendcodebyemail = (id,email)->
        Math.floor 2*Math.random()

  class Publisher extends ServerModels
    constructor: (d) ->
      super(d)
      self = @

      @name = ko.observable(d && d.name)

      @active = ko.observable(d && d.active)

  class Admin extends ServerModels
    constructor: (d) ->
      super(d)
      self = @

      @name = ko.observable(d && d.name)

      @email = ko.observable(d && d.email)

      @roles = ko.observableArray(d && d.roles)

      @publishers = ko.observableArray(((d && d.publishers) || []).map((v) ->
        new Publisher v
      ))

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
  PathTarget: PathTarget,
  Publisher: Publisher,
  truncateToDay: truncateToDay,
  datetostr: datetostr,
  strtodate: strtodate }
)