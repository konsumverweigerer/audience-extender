requirejs.config
  shim:
    'jquery':
      deps: []
      exports: '$'
    'bootstrap':
      deps: [ 'jquery' ]
    'jqBootstrapValidation':
      deps: [ 'jquery', 'bootstrap' ]
    'bootstrap-datepicker':
      deps: [ 'jquery', 'bootstrap' ]
    'bootstrap-editable':
      deps: [ 'jquery', 'bootstrap' ]
    'jqBootstrapValidation':
      deps: [ 'jquery', 'bootstrap' ]
    'jsRoutes':
      deps: []
      exports: 'jsRoutes'

define("jquery", [ "webjars!jquery.js" ], -> $)
define("bootstrap", [ "webjars!bootstrap.js" ], -> )
define("knockout", [ "webjars!knockout.js" ], (ko) -> ko)
define("jquery.dataTables", [ "webjars!jquery.dataTables.js" ], -> )
define("bootstrap-datepicker", [ "webjars!bootstrap-datepicker.js" ], -> )
define("bootstrap-editable", [ "webjars!bootstrap-editable.js" ], -> )
define("jqBootstrapValidation", [ "webjars!jqBootstrapValidation.js" ], -> )

define("jsRoutes", ["/routes.js"], -> )

require(["knockout", "lib/models", "jquery", "bootstrap",
"lib/knockout-datatables", 
"lib/knockout-editable", 
"lib/knockout-datepicker", 
"lib/knockout-jqbootstrapvalidation", 
"lib/knockout-misc", 
"jsRoutes"], (ko, mod) ->
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
