require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!bootstrap.js", "lib/knockout-editable", "/routes.js"], (ko, mod) ->
  class Admins
    constructor: (d) ->
      self = @

      @datatablescroller = new mod.Scroller

      @datatable = new mod.Datatable(["name","email","roles","publishers"])

      @admins = ko.observableArray([])

      @messages = ko.observableArray([])

  models = new Admins
    
  ko.applyBindings(models)
  
  window.models = models
  #init

  if window.data && window.data.admins
    window.data.admins.map( (p,i) ->
      pm = new mod.Admin(p)
      models.admins.push(pm)
      if p.active == "true"
        models.admin(pm)
    )

  require(["webjars!jquery.dataTables.js", "lib/datatables-ext"], () ->
    datatable = $('table.data-table').dataTable({
      bLengthChange: false,
      aoColumns: [
        { sType: "string" },
        { sType: "string" },
        { sType: "string" },
        { sType: "string" }
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
    models.datatable.rows.subscribe((nv)->
      datatable.fnClearTable()
      nv.map( (n,i) ->
        datatable.fnAddData(n)
      )
    )
    models.datatable.data(models.admins())
  )
)
