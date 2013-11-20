require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!d3.v2.js", "webjars!bootstrap.js",
"ext/jquery.jcarousel", "ext/bootstrap-slider", "lib/knockout-editable", "lib/knockout-datepicker", "lib/knockout-nvd3", "lib/knockout-datatables",
"/routes.js"], (ko, mod) ->
  class Dashboard
    constructor: (d) ->
      self = @

      @campaignchartdaterange = new mod.DateRange

      @campaigntablescroller = new mod.Scroller

      @campaigntablesearchbar = new mod.Searchbar

      @campaignchartdata = new mod.Chartdata

      @campaigndatatable = new mod.Datatable(["name","state","revenue","cost","from","to"])

      @publisher = ko.observable()

      @publishers = ko.observableArray([])

      @campaignstep = ko.observable(1)

      @currentcampaign = ko.observable(new mod.Campaign({name:''}))

      @messages = ko.observableArray([])

      @resetstep = () ->
        {}

      @nextstep = () ->
        {}

      @prevstep = () ->
        {}

      @newcampaign = () ->
        self.currentcampaign(new mod.Campaign({name:'New Campaign'}))

      @savecampaign = () ->
        a = self.currentcampaign()
        if a.id()
          alert('update campaign')
        else
          alert('persist campaign')

  models = new Dashboard

  ko.applyBindings(models)

  window.models = models
  #init

  window.data.publishers.map( (p,i) ->
    pm = new mod.Publisher(p)
    models.publishers.push(pm)
    if p.active == "true"
      models.publisher(pm)
  )
  if !models.publisher() && models.publishers().length
  	models.publisher(models.publishers()[0])

  models.messages.push(new mod.Message('Kampagne MegaAudiences läuft bald aus',
        'Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
        sed diam nonumy eirmod tempor invidunt ut labore et dolore
        magna aliquyam erat, sed diam voluptua. At vero eos et accusam
        et justo duo dolores et ea rebum. Stet clita kasd gubergren,
        no sea takimata sanctus est Lorem ipsum dolor sit amet.','info'))
  models.chartdaterange.loadData()

  require(["webjars!nv.d3.js"], () ->
    data = ->
      m = (models.chartdaterange.endDate()-models.chartdaterange.startDate())/(24*60*60*1000)
      tf = 'days'
      if m==1
        m = 24
        tf = 'hours'
      return stream_layers(3,m,.1).map( (data, i) ->
        if i==0
          s = 'Ad Spend'
          t = 'adspend'
        if i==1
          s = 'Revenue'
          t = 'revenue'
        if i==2
          s = 'Profit'
          t = 'profit'
        return {
          key: s,
          cls: t,
          values: data,
          timeframe: tf
        }
      )

    stream_index = (d, i) ->
      m = (models.chartdaterange.endDate()-models.chartdaterange.startDate())/(24*60*60*1000)
      if m==1
        i = models.chartdaterange.startDate().getTime()+(i*60*60*1000)
      else
        i = models.chartdaterange.startDate().getTime()+(i*24*60*60*1000)
      {x: i, y: 100*Math.max(0, d)}

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
    require(["webjars!bootstrap-datepicker.js"], () ->
      $('.input-daterange').datepicker({
        calendarWeeks: true,
        autoclose: true,
        todayHighlight: true
      });
      models.chartdaterange.dataloader = () ->
        nv.graphs.pop()
        nv.addGraph(() ->
          currentdata = data()
          models.chartdata.charts(currentdata)
          chart = nv.models.lineChart()
          chart.xAxis.showMaxMin(false).staggerLabels(true)
            .tickFormat((d) ->
              d3.time.format(models.chartdaterange.format())(new Date(d))
            );
          chart.yAxis.showMaxMin(false)
            .tickFormat((d) ->
              '$'+(d3.format('.2f'))(d)
            );
          chart.color(['#bf0c0c','#275980','#f3b300'])
          d3.select('#chart svg')
            .datum(currentdata)
            .transition().duration(500).call(chart)
          nv.utils.windowResize(chart.update)
          return chart
        )
      models.chartdaterange.dateRange.subscribe((nv)->
        $('input.date-picker[name=start]').datepicker('setDate',models.chartdaterange.startDate())
        $('input.date-picker[name=end]').datepicker('setDate',models.chartdaterange.endDate())
      )
      $('input.date-picker').on('changeDate',(v) ->
        if v.target.name == 'start'
          models.chartdaterange.formattedStartDate(datetostr(v.date))
        if v.target.name == 'end'
          models.chartdaterange.formattedEndDate(datetostr(v.date))
      )
      models.chartdaterange.dateRange('Last Day')
    )
    require(["webjars!jquery.dataTables.js"], () ->
      $.extend($.fn.dataTableExt.oSort, {
        'currency-asc': (a,b) ->
          a-b
        'currency-desc': (a,b) ->
          b-a
        'currency-pre': (a) ->
          if a=='-'
            return 0
          parseFloat(a.replace(/[^\d\-\.]/g,''))
        'percent-asc': (a,b) ->
          a-b
        'percent-desc': (a,b) ->
          b-a
        'percent-pre': (a) ->
          if a==''
            return 0
          parseFloat(a.replace(/[^\d\-\.]/g,''))
      })
      datatable = $('table.data-table').dataTable({
        bLengthChange: false,
        aoColumns: [
          { sType: "string" },
          { sType: "percent" },
          { sType: "currency" },
          { sType: "currency" },
          { sType: "date" },
          { sType: "date" }
        ],
        sDom: 'lrti',
        fnInfoCallback: (oSettings, iStart, iEnd, iMax, iTotal, sPre) ->
          dl = oSettings._iDisplayLength
          models.datatablescroller.pageSize(dl)
          models.datatablescroller.fromIndex(iStart)
          models.datatablescroller.maxIndex(iMax)
          return ''
      });
      models.datatablescroller.currentPage.subscribe((nv)->
        datatable.fnPageChange(nv-1)
      )
      models.datatablesearchbar.filldata = () ->
        datatable.fnClearTable()
        i = 0
        n = new Date()
        m = 40+Math.ceil(100*Math.random())
        while i++ < m
          d = mod.truncateToDay(n,Math.ceil(10*Math.random()),-Math.ceil(10*Math.random()))
          datatable.fnAddData(['Name '+Math.ceil(1000*Math.random()),(100*Math.random()).toFixed(1)+'%',
            '$'+(100*Math.random()).toFixed(2),'$'+(10*Math.random()).toFixed(2),mod.datetostr(d[0]),mod.datetostr(d[1])])
    )
  )
)
