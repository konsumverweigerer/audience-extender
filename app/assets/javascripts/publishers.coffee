define("jquery", [ "webjars!jquery.js" ], -> $ )

require(["webjars!knockout.js", "lib/models", "webjars!jquery.js", "webjars!bootstrap.js",
"lib/knockout-misc", "lib/knockout-datatables", "/routes.js"], (ko, mod) ->
  class Publishers
    constructor: (d) ->
      self = @

      @loader = new mod.Counter {wrap:false,minValue:0}
      @alert = new mod.Message()
      @messages = ko.observableArray []

      @datatablescroller = new mod.Scroller
      @datatable = new mod.Datatable ["name","admins"]

      @publishers = ko.observableArray []

  models = new Publishers
    
  ko.applyBindings models
  
  window.models = models
  #init

  if window.data && window.data.publishers
    for p in window.data.publishers
      pu = new mod.Publisher p
      models.publishers.push pu
  models.datatable.data models.publishers()
)