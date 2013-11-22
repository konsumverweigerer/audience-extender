define(["webjars!knockout.js"], (ko) ->
  day = 24*60*60*1000

  ranges = [{ name: 'Last Day', from: 2, to: 1, unit: 'day' },
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

  rangeNames = () ->
    a = ['']
    ranges.map( (n,i) ->
      a.push(n.name)
    )
    return a

  class DateRange
    constructor: () ->
      self = @

      @availableDateRanges = ko.observableArray(rangeNames())

      @startDateRaw = ko.observable(truncateToDay(new Date(),2,1)[0])

      @startDate = ko.computed({
        read: () ->
          self.startDateRaw()
        write: (v) ->
          old = self.startDateRaw()
          self.startDateRaw(v)
          if old!=self.startDateRaw()
            self.loadData()
        owner: self
      })

      @endDateRaw = ko.observable(truncateToDay(new Date(),2,1)[1])

      @endDate = ko.computed({
        read: () ->
          self.endDateRaw()
        write: (v) ->
          old = self.endDateRaw()
          self.endDateRaw(v)
          if old!=self.endDateRaw()
            self.loadData()
        owner: self
      })

      @format = ko.computed(()->
        if self.endDate().getTime()-self.startDate().getTime() < 2*day
          return '%H:%M'
        return '%m/%d/%Y'
      )

      @formattedStartDate = ko.computed({
        read: () ->
          datetostr(self.startDate())
        write: (v) ->
          self.startDate(strtodate(v))
        owner: self
      })

      @formattedEndDate = ko.computed({
        read: () ->
          datetostr(self.endDate())
        write: (v) ->
          self.endDate(strtodate(v))
        owner: self
      })

      @dates = ko.computed({
      	read: () ->
      	  [self.startDate(),self.endDate()]
      	write: (v) ->
      	  if v.length == 2
      	    self.startDate(v[0])
      	    self.endDate(v[1])
      	owner: self,
      	deferEvaluation: true
      })

      @dateRange = ko.computed({
      	read: () ->
      	  v = ''
      	  nd = new Date()
      	  sd = self.startDate()
      	  ed = self.endDate()
      	  ranges.map( (n,i)->
      	    t = truncateToDay(nd,n.from,n.to,n.unit)
      	    if Math.abs(t[0].getTime()-sd.getTime())<3600001 && Math.abs(t[1].getTime()-ed.getTime())<3600001
      	      v = n.name
      	  )
      	  return v
      	write: (v) ->
          ranges.map( (n,i)->
            if v == n.name
              t = truncateToDay(new Date(),n.from,n.to,n.unit)
              self.startDate(t[0])
              self.endDate(t[1])
          )
      	owner: self,
      	deferEvaluation: true
      })

      @lastDay = () ->
        t = truncateToDay(new Date(),2,1)
        self.startDate(t[0])
        self.endDate(t[1])

      @lastWeek = () ->
        t = truncateToDay(new Date(),2,1,'week')
        self.startDate(t[0])
        self.endDate(t[1])

      @loadData = () ->
        self.dataloader()

      @dataloader = () ->
        {}

  class Scroller
    constructor: () ->
      self = @

      @fromIndex = ko.observable(1)

      @maxIndex = ko.observable(0)

      @shownPages = ko.observable(5)

      @pageSize = ko.observable(10)

      @currentPage = ko.computed(() ->
        Math.ceil(self.fromIndex() / self.pageSize())
      )

      @toIndex = ko.computed(() ->
        Math.min(self.maxIndex(),self.fromIndex()+self.pageSize()-1)
      )

      @maxPage = ko.computed(() ->
        Math.ceil(self.maxIndex() / self.pageSize())
      )

      @hasData = ko.computed(() ->
        self.maxIndex() > 0
      )

      @hasNoPrev = ko.computed(() ->
        self.currentPage() < 2
      )

      @hasNoNext = ko.computed(() ->
        self.currentPage() >= self.maxPage()
      )

      @availablePages = ko.computed(() ->
        a = []
        i = 1
        m = self.maxPage()
        while i <= m
          a[i-1] = i++
        return a
      )

      @visiblePages = ko.computed(()->
        p = []
        i = 1
        mp = self.maxPage()
        sp = self.shownPages()
        cp = self.currentPage()
        while (i + (sp/2) - 1) < cp && (i + sp - 1) < mp
          i++
        j = 0
        while j < sp && i+j <= mp
          p[j] = {
            page: i+j,
            active: (i+j)==cp
          }
          j++
        return p
      )

      @previous = () ->
        c = self.fromIndex()
        ps = self.pageSize()
        if c > ps
          self.fromIndex(c-ps)

      @next = () ->
        c = self.fromIndex()
        ps = self.pageSize()
        mp = self.maxIndex()
        if c+ps <= mp
          self.fromIndex(c+ps)

      @gotoPage = () ->
        ps = self.pageSize()
        mp = self.maxIndex()
        ni = 1+((@page-1)*ps)
        if ni>0 && ni<=mp
          self.fromIndex(ni)

  class Searchbar
    constructor: () ->
      self = @

      @availableCategories = ko.observableArray(['Status 1','Status 2','Status 3'])

      @category = ko.observable('')

      @query = ko.observable('')

      @search = () ->
        self.filldata()

      @filldata = () ->
        {}

  class Chartdata
    constructor: () ->
      self = @

      @chartcontentRaw = ko.observableArray([
        {key: 'Revenue', values: [{x:0,y:5000}]},
        {key: 'Ad spend', values: [{x:0,y:4000}]},
        {key: 'Profit', values: [{x:0,y:3000}]}])

      @chartcontent = ko.computed({
        read: () ->
          self.chartcontentRaw()
        write: (v) ->
          self.chartcontentRaw(v)
        owner: self
      })

      @sums = ko.computed(() ->
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
        if f.length == 0
          return 0
        f[0]

      @sumadspend = ko.computed(() ->
        self.calcsum('adspend').toFixed(0)
      )

      @sumrevenue = ko.computed(() ->
        self.calcsum('revenue').toFixed(0)
      )

      @sumprofit = ko.computed(() ->
        self.calcsum('profit').toFixed(0)
      )

  class Datatable
    constructor: (headers, mapper) ->
      self = @

      @headers = ko.observableArray(headers || [])

      @data = ko.observableArray([])

      @mapper = ko.observable(mapper || {})

      @rows = ko.computed(() ->
        h = self.headers()
        mm = self.mapper()
        self.data().map( (n,i) ->
          h.map( (m,j) ->
            dat = ''
            if n.indexOf
              dat = n[h.indexOf(m)]
            else
              dat = n[m]
              if dat.call
                dat = dat()
            if mm && mm[m]
              mm[m](dat)
            else
              dat
          )
        )
      )
      
      @rowClick = (d) ->
        {}

  class Message
    constructor: (dortitle,content,priority) ->
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

  class Campaign
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @name = ko.observable(d && d.name)

      @state = ko.observable(d && d.state)

      @revenue = ko.observable(d && d.revenue)

      @cost = ko.observable(d && d.cost)

      @from = ko.observable(d && d.from)

      @to = ko.observable(d && d.to)

  class Audience
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @name = ko.observable(d && d.name)

      @tracking = ko.observable(d && d.tracking)

      @websites = ko.observableArray(d && d.websites)

      @paths = ko.observableArray(d && d.paths)

  class Website
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @name = ko.observable(d && d.name)

      @active = ko.observableArray(d && d.audiences)

      @url = ko.observable(d && d.url)

  class PathTarget
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @variant = ko.observable(d && d.variant)

      @path = ko.observable(d && d.path)

      @website = ko.observable(d && d.website)

  class Publisher
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @name = ko.observable(d && d.name)

      @active = ko.observable(d && d.active)

  class Admin
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @name = ko.observable(d && d.name)

      @email = ko.observable(d && d.email)

      @roles = ko.observableArray(d && d.roles)

      @publishers = ko.observableArray(((d && d.publishers) || []).map((v) ->
        new Publisher(v)
      ))

  { Message: Message,
  Datatable: Datatable,
  Chartdata: Chartdata,
  Searchbar: Searchbar,
  Scroller: Scroller,
  DateRange: DateRange,
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