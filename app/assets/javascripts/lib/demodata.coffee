define(["webjars!knockout.js"], (ko) ->
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
          for i in [0..m]
            w = (i/m-y)*z
            a[i] += x*Math.exp(-w*w)
        return d3.range(n).map( ->
          a = []
          for i in [0..m]
            a[i] = o+o*Math.random()
          for i in [0..5]
            bump(a)
          return a.map((r,s) ->
            stream_index(r,s,idxf))
        )

      models.audiencechartdaterange.dataloader = ->
        models.audiencechart.chartcontent data()
      models.audiencechartdaterange.dateRange 'Last Day'

      models.audiencetablesearchbar.filldata = ->
        n = new Date()
        m = 40+Math.ceil(100*Math.random())
        val = []
        for i in [0..m]
          d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
          val[i++] = new mod.Audience
            name:'Audience '+Math.ceil(1000*Math.random())
            state: ['paused','active','pending','cancelled'][(Math.floor(4*Math.random()))]
            websites: [1..(Math.ceil(10*Math.random()))]
            count: Math.ceil 10000*Math.random()
        models.audiencetable.data val
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
          for i in [0..m]
            w = (i/m-y)*z
            a[i] += x*Math.exp(-w*w)
        return d3.range(n).map( ->
          a = []
          for i in [0..m]
            a[i] = o+o*Math.random()
          for i in [0..5]
            bump(a)
          return a.map((r,s) ->
            stream_index(r,s,idxf))
        )

      models.campaignchartdaterange.dataloader = ->
        models.campaignchart.chartcontent data()
      models.campaignchartdaterange.dateRange 'Last Day'
  
      models.campaigntablesearchbar.filldata = ->
        n = new Date()
        m = 40+Math.ceil(100*Math.random())
        val = []
        for i in [0..m]
          d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
          val[i++] = new mod.Campaign
            name: 'Campaign '+Math.ceil(1000*Math.random())
            state: ['paused','finished','active','pending','cancelled','rejected',Math.floor(100*Math.random())+'%'][(Math.floor(7*Math.random()))]
            revenue: (100*Math.random())
            cost: (10*Math.random())
            from: d[0]
            to: d[1]
        models.campaigntable.data val
    )

  generatewebsites = (mod,models) ->
    ws = models.websites()
    m = 40+Math.ceil(100*Math.random())
    for i in [1...m]
      ws.push(new mod.Website({id:i,name:'Website '+i,code:'<script src="http://test/my/code/'+(Math.ceil(10000*Math.random()))+'" type="text/javascript"></script>'}))
    
  { generate: (mod,models,page) ->
    if 'audience'==page
      generatewebsites(mod,models)
      generateaudience(mod,models)
    else if 'audience'==page
      generatecampaign(mod,models)
  }
)