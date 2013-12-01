requirejs.config
  shim:
    'jquery':
      deps: []
      exports: '$'
    'bootstrap':
      deps: [ 'jquery' ]
define("jquery", [ "webjars!jquery.js" ], -> $ )
define("bootstrap", [ "webjars!bootstrap.js" ], -> )

require(["pages/app"], -> )
