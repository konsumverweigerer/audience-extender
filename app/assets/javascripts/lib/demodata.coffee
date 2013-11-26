define(["webjars!knockout.js"], (ko) ->
  generateaudiences = (mod,models) ->
    n = new Date()
    m = 40+Math.ceil(100*Math.random())
    q = Math.ceil(10*Math.random())
    val = []
    j = 1
    for i in [0..m]
      d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
      ws = [1..(Math.ceil(10*Math.random()))]
      ps = ({ id: j++, path: '/my/'+l+'/path', active: ['on','off'][(Math.floor(2*Math.random()))], website: ws[(Math.floor(ws.length*Math.random()))]} for l in [0..q])
      val[i++] = new mod.Audience
        id: i
        name:'Audience '+Math.ceil 1000*Math.random()
        state: ['paused','active','pending','cancelled'][(Math.floor(4*Math.random()))]
        websites: ws
        count: Math.ceil 10000*Math.random()
        paths: ps
    models.audiences val

  generatecampaigns = (mod,models) ->
    n = new Date()
    m = 40+Math.ceil 100*Math.random()
    val = []
    for i in [0..m]
      d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
      val[i++] = new mod.Campaign
        id: i
        name: 'Campaign '+Math.ceil 1000*Math.random()
        state: ['paused','finished','active','pending','cancelled','rejected',Math.floor(100*Math.random())+'%'][(Math.floor(7*Math.random()))]
        revenue: (100*Math.random())
        cost: (10*Math.random())
        from: d[0]
        to: d[1]
    models.campaigns val
      
  generatepackages = (mod,models) ->
    m = 5+Math.ceil 10*Math.random()
    val = []
    for i in [0..m]
      val[i++] = new mod.Package
        id: i
        name: 'Package '+Math.ceil 1000*Math.random()
        count: 1000*(i+1)
    models.packages val

  generateaudience = (mod,models) ->
    require(["webjars!nv.d3.js"], ->
      data = ->
        m = (models.audiencechartdaterange.endDate()-models.audiencechartdaterange.startDate())/(24*60*60*1000)
        sd = models.audiencechartdaterange.startDate().getTime()
        idxf = (i) ->
          if m<2
            i = sd+(i*60*60*1000)
          else
            i = sd+(i*24*60*60*1000)
        [tf,mn] = ['days',m]
        if m<2
          [tf,mn] = ['hours',24]
        return stream_layers(9,mn,.1,idxf).map((data, i) ->
          {
            key: 'Audience '+i
            cls: ''
            values: data
            timeframe: tf
          }
        )

      stream_index = (d,i,idxf) ->
        {x: idxf(i), y: 100*Math.max(0, d)}

      stream_layers = (n,m,o,idxf) ->
        if arguments.length<3
          o = 0
        bump = (a) ->
          x = 1/(.1+Math.random())
          y = 2*Math.random()-.5
          z = 10/(.1+Math.random())
          for i in [0...m]
            w = (i/m-y)*z
            a[i] += x*Math.exp(-w*w)
        return d3.range(n).map( ->
          a = []
          for i in [0...m]
            a[i] = o+o*Math.random()
          for i in [0...5]
            bump(a)
          return a.map((r,s) ->
            stream_index(r,s,idxf))
        )

      models.audiencechartdaterange.dataloader = ->
        models.audiencechart.chartcontent data()
      models.audiencechartdaterange.dateRange 'Last Day'

      v.refresh models.websites() for v in models.audiences()

      models.audiencetablesearchbar.filldata = ->
        #todo: filter
        models.audiencetable.data models.audiences()

      models.audiencetablesearchbar.search()
    )
    
  generatecampaign = (mod,models) ->
    require(["webjars!nv.d3.js"], ->
      data = ->
        m = (models.campaignchartdaterange.endDate()-models.campaignchartdaterange.startDate())/(24*60*60*1000)
        sd = models.campaignchartdaterange.startDate().getTime()
        idxf = (i) ->
          if m<2
            i = sd+(i*60*60*1000)
          else
            i = sd+(i*24*60*60*1000)
        [tf,mn] = ['days',m]
        if m<2
          [tf,mn] = ['hours',24]
        dat = stream_layers(2,mn,.1,idxf).map( (data, i) ->
          if i==0
            [s,t] = ['Revenue','revenue']
          else if i==1
            [s,t] = ['Ad Spend','adspend']
          {
            key: s
            cls: t
            values: data.map((n,j) ->
              if i==1
                n.y = -n.y/5
              if tf=='hours'
                n.y = n.y/24
              n
            )
            timeframe: tf
          }
        )
        dat[0].values[0].y=0;
        dat[1].values[0].y=0;
        dat[2] = 
          key: 'Profit'
          cls: 'profit'
          values: sumdata(dat[0].values,dat[1].values)
          timeframe: tf
        dat

      sumdata = (v, w) ->
        if v.length==w.length
          v.map((n,i) ->
            {x:n.x,y:n.y+w[i].y}
          )
        else
          []
    
      stream_index = (d, i, idxf) ->
        {x: idxf(i), y: 100*Math.max(0, d)}

      stream_layers = (n, m, o, idxf) ->
        if arguments.length < 3
          o = 0
        bump = (a) ->
          x = 1/(.1+Math.random())
          y = 2*Math.random()-.5
          z = 10/(.1+Math.random())
          for i in [0...m]
            w = (i/m-y)*z
            a[i] += x*Math.exp(-w*w)
        return d3.range(n).map( ->
          a = []
          for i in [0...m]
            a[i] = o+o*Math.random()
          for i in [0...5]
            bump(a)
          return a.map((r,s) ->
            stream_index(r,s,idxf))
        )

      models.campaignchartdaterange.dataloader = ->
        models.campaignchart.chartcontent data()
      models.campaignchartdaterange.dateRange 'Last Day'
  
      models.campaigntablesearchbar.filldata = ->
        #todo: filter
        models.campaigntable.data models.campaigns()

      models.campaigntablesearchbar.search()
    )

  generatewebsites = (mod,models) ->
    ws = models.websites()
    m = 10+Math.ceil(10*Math.random())
    for i in [1...m]
      w = new mod.Website
        id:i
        name:'Website '+i
        count: Math.ceil 100000*Math.random()
        code:'<script src="http://test/my/code/'+(Math.ceil(10000*Math.random()))+'" type="text/javascript"></script>'
      ws.push(w)
    return
    
  { generate: (mod,models,page) ->
    if 'audience'==page
      generatewebsites(mod,models)
      generateaudiences(mod,models)
      generateaudience(mod,models)
    else if 'campaign'==page
      generatepackages(mod,models)
      generateaudiences(mod,models)
      generatecampaigns(mod,models)
      generatecampaign(mod,models)
  }
)