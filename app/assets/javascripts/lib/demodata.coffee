define(["knockout"], (ko) ->
  rnd = (f,t) -> f+Math.floor((1+t-f)*Math.random())

  generateaudiences = (mod,models) ->
    n = new Date()
    val = []
    j = 1
    for i in [0..(rnd(40,140))]
      d = mod.truncateToDay(n,rnd(1,10),-rnd(1,10))
      ws = [1..(rnd(1,10))]
      ps = ({id: j++, path: '/my/'+l+'/path',active: ['on','off'][rnd(0,1)],website: ws[rnd(1,ws.length)-1]} for l in [0..(rnd(1,10))])
      val[i++] = new mod.Audience
        id: i
        name:'Audience '+rnd(1,1000)
        state: ['paused','active','pending','cancelled'][rnd(0,3)]
        websites: ws
        count: Math.ceil 10000*Math.random()
        paths: ps
    models.audiences val

  generatecampaigns = (mod,models) ->
    n = new Date()
    val = []
    for i in [0..(rnd(40,140))]
      d = mod.truncateToDay(n,rnd(1,10),-rnd(1,10))
      au = [1...(rnd(1,3))]
      cr = ((new mod.Creative {id:j,name:'Creative '+rnd(1,1000),url: '/assets/images/thumbnail-site.gif'}) for j in [1...(rnd(1,3))])
      val[i++] = new mod.Campaign
        id: i
        name: 'Campaign '+rnd(1,1000)
        state: ['paused','finished','active','pending','cancelled','rejected',rnd(0,99)+'%'][rnd(0,6)]
        revenue: 100*Math.random()
        cost: 10*Math.random()
        from: d[0]
        to: d[1]
        audiences: au
        creatives: cr
        package: rnd(0,5) || undefined
    models.campaigns val

  generatepackages = (mod,models) ->
    val = []
    for i in [0..(rnd(5,15))]
      val[i++] = new mod.Package
        id: i
        name: 'Package '+rnd(1,1000)
        count: 1000*(i+1)
        salesCpm: 20+10*Math.random()
        buyCpm: 10+10*Math.random()
        reach: rnd(1000,20000)
    models.packages val

  generateaudience = (mod,models) ->
    data = -> []
    models.audiencechartdaterange.dataloader = ->
      models.audiencechart.chartcontent data()
    v.refresh models.websites() for v in models.audiences()
    
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
        {x: idxf(i),y: 100*Math.max(0,d)}

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
          bump a for i in [0...5]
          return a.map((r,s) -> stream_index(r,s,idxf))
        )
      models.audiencechart.chartcontent data()
    )
    
  generatecampaign = (mod,models) ->
    data = -> []
    models.campaignchartdaterange.dataloader = ->
      models.campaignchart.chartcontent data()

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
        dat[0].values[0].y = 0
        dat[1].values[0].y = 0
        dat[2] = 
          key: 'Profit'
          cls: 'profit'
          values: sumdata(dat[0].values,dat[1].values)
          timeframe: tf
        dat

      sumdata = (v, w) ->
        if v.length==w.length
          v.map((n,i) -> {x:n.x,y:n.y+w[i].y})
        else
          []
    
      stream_index = (d, i, idxf) ->
        {x: idxf(i),y: 100*Math.max(0, d)}

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
          a[i] = o+o*Math.random() for i in [0...m]
          bump a for i in [0...5]
          return a.map((r,s) -> stream_index(r,s,idxf))
        )
      models.campaignchart.chartcontent data()
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