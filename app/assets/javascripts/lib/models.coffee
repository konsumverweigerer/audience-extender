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
      
      @startDate = ko.observable(truncateToDay(new Date(),2,1)[0])
      
      @endDate = ko.observable(truncateToDay(new Date(),2,1)[1])
      
      @format = ko.computed(()->
        if self.endDate().getTime()-self.startDate().getTime() < 2*day
          return '%H:%M'
        return '%m/%d/%Y'
      )
  
      @formattedStartDate = ko.computed({
        read: () ->
          datetostr(self.startDate())
        write: (v) ->
          old = self.startDate()
          self.startDate(strtodate(v))
          if old!=self.startDate()
            self.loadData()
        owner: self
      })

      @formattedEndDate = ko.computed({
        read: () ->
          datetostr(self.endDate())
        write: (v) ->
          old = self.endDate()
          self.endDate(strtodate(v))
          if old!=self.endDate()
            self.loadData()
        owner: self
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
              self.loadData()
            return
          )
          return
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

      @charts = ko.observableArray([
        {key: 'Revenue', values: [{x:0,y:5000}]},
        {key: 'Ad spend', values: [{x:0,y:4000}]},
        {key: 'Profit', values: [{x:0,y:3000}]}])

      @sums = ko.computed(() ->
        self.charts().map((n,i) ->
          n.values.map((p,j) -> 
            p.y
          ).reduce((x,y) ->
            x+y
          , 0
          )
        )
      )

      @calcsum = (cls) ->
        f = self.charts().filter((n,i) ->
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
    constructor: () ->
      self = @

      @rows = ko.observableArray([])
      
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

  class Audience
    constructor: (d) ->
      self = @

      @id = ko.observable(d && d.id)

      @name = ko.observable(d && d.name)

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

      @publishers = ko.observable(d && d.publishers.map((v) ->
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
  Admin: Admin,
  Publisher: Publisher,
  truncateToDay: truncateToDay,
  datetostr: datetostr,
  strtodate: strtodate }
)