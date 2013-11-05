require(["webjars!knockout.js", "webjars!jquery.js", 
    "webjars!bootstrap.js", "webjars!d3.v2.js", "webjars!nv.d3.js",
    "/routes.js"], (ko) ->
  data = ->
    return stream_layers(3,10+Math.random()*100,.1).map( (data, i) ->
      return {
        key: 'Stream' + i,
        values: data
      };
    );

  stream_layers = (n, m, o) -> 
    if arguments.length < 3
      o = 0    
    bump = (a) ->
      x = 1 / (.1 + Math.random())
      y = 2 * Math.random() - .5
      z = 10 / (.1 + Math.random())
      i = 0
      while i < m
        w = (i / m - y) * z
        a[i++] += x * Math.exp(-w * w)
    return d3.range(n).map( () ->
        a = []
        i = 0
        while i < m
          a[i++] = o + o * Math.random()
        i = 0
        while i++ < 5
          bump(a)
        return a.map(stream_index)
    );

  stream_waves = (n, m) ->
    return d3.range(n).map((i) ->
      return d3.range(m).map((j) ->
          x = 20 * j / m - i / 3
          return 2 * x * Math.exp(-.5 * x)
        ).map(stream_index);
      );

  stream_index = (d, i) ->
    return {x: i, y: Math.max(0, d)};

  nv.addGraph(() ->
    chart = nv.models.multiBarChart()
    chart.xAxis
        .tickFormat(d3.format(',f'));
    chart.yAxis
        .tickFormat(d3.format(',.1f'));
    d3.select('#chart svg')
        .datum(data())
      .transition().duration(500).call(chart);
    nv.utils.windowResize(chart.update);
    return chart;
  );
)
