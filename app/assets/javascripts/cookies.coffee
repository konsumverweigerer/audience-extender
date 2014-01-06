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
define("momentjs", [ "webjars!moment.js" ], -> )

define("jsRoutes", ["/routes.js"], -> )

require(["knockout", "jquery", "bootstrap",
"pages/cookies"], (ko) ->

)
