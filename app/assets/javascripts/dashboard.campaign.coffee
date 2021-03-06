requirejs.config
  map:
    '/webjars/jquery-file-upload/8.4.2/js':
      '/webjars/jquery-file-upload/8.4.2/js/./jquery.fileupload': 'webjars!jquery.fileupload.js'
      '/webjars/jquery-file-upload/8.4.2/js/./jquery.fileupload-process': 'webjars!jquery.fileupload-process.js'
  shim:
    'jquery':
      deps: []
      exports: '$'
    'bootstrap':
      deps: [ 'jquery' ]
    'jquery.ui.widget.js':
      deps: [ 'jquery' ]
    'jqBootstrapValidation':
      deps: [ 'jquery', 'bootstrap' ]
    'nv.d3':
      deps: [ 'd3' ]
      exports: 'nv'
    'd3':
      deps: [ '' ]
      exports: 'd3'
    'bootstrap-datepicker':
      deps: [ 'jquery', 'bootstrap' ]
    'bootstrap-tour':
      deps: [ 'jquery', 'bootstrap' ]
    'chardin':
      deps: [ 'jquery' ]
    'bootstrap-editable':
      deps: [ 'jquery', 'bootstrap' ]
    'jqBootstrapValidation':
      deps: [ 'jquery', 'bootstrap' ]
    'jsRoutes':
      deps: []
      exports: 'jsRoutes'

define("jquery", [ "webjars!jquery.js" ], -> $)
define("bootstrap", [ "webjars!bootstrap.js" ], -> )
define("knockout", [ "webjars!knockout.js" ], (ko) -> ko )
define("jquery.dataTables", [ "webjars!jquery.dataTables.js" ], -> )
define("bootstrap-datepicker", [ "webjars!bootstrap-datepicker.js" ], -> )
define("bootstrap-editable", [ "webjars!bootstrap-editable.js" ], -> )
define("jqBootstrapValidation", [ "webjars!jqBootstrapValidation.js" ], -> )
define("jquery.ui.widget", [ "webjars!jquery.ui.widget.js" ], -> )
define("jquery.fileupload", [ "webjars!jquery.fileupload.js" ], -> )
define("jquery.iframe-transport", [ "webjars!jquery.iframe-transport" ], -> )
define("jquery.fileupload-image", [ "webjars!jquery.fileupload-image.js" ], -> )
define("jquery.fileupload-process", [ "webjars!jquery.fileupload-process.js" ], -> )
define("jquery.fileupload-validate", [ "webjars!jquery.fileupload-validate.js" ], -> )
define("d3", [ "webjars!d3.js" ], -> d3 )
define("nv.d3", [ "webjars!nv.d3.js" ], -> nv )
define("momentjs", [ "webjars!moment.js" ], -> )

define("chardin", [ "ext/chardinjs" ], -> )
define("bootstrap-tour", [ "ext/bootstrap-tour" ], -> )
define("load-image", [ "ext/load-image" ], (loadImage) -> loadImage)
define("load-image-meta", [ "ext/load-image-meta" ], -> )
define("load-image-exif", [ "ext/load-image-exif" ], -> )
define("load-image-ios", [ "ext/load-image-ios" ], -> )
define("canvas-to-blob", [ "ext/canvas-to-blob" ], -> )

define("jsRoutes", ["/routes.js"], -> )

require(["knockout", "jquery", "bootstrap",
"pages/dashboard.campaign"], (ko) ->
)
