define("jquery", [ "webjars!jquery.js" ], -> $ )

require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!bootstrap.js",
"lib/knockout-misc", "lib/knockout-datatables", "/routes.js"], (ko, mod) ->
  class Admins
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable ["name","email","roles","publishers"]

      @admins = ko.observableArray []

  models = new Admins
    
  ko.applyBindings models
  
  window.models = models
  #init

  if window.data && window.data.admins
    for a in window.data.admins
      ad = new mod.Admin a
      models.admins.push ad
  models.datatable.data models.admins()
)
