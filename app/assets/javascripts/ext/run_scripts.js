function ex_scripts(delem) {
  function nodeName(elem, name) {
    return elem.nodeName && elem.nodeName.toUpperCase() === name.toUpperCase();
  };

  function evalScript(scr) {
    var elem = scr[0], data = (elem.text || elem.textContent || elem.innerHTML || "" ),
        head = ((document.getElementsByTagName('head') || [null])[0] || document.documentElement),
        script = document.createElement("script");

    script.type = scr[1];
    try {
      script.appendChild(document.createTextNode(data));      
    } catch(e) {
      script.text = data;
    }

    head.appendChild(script);
  };

  var scripts = [], script, dnodes = delem.childNodes, child, i;

  for (i = 0; dnodes[i]; i++) {
    child = dnodes[i];
    if (nodeName(child, "script" )) {
    	scripts.push([child,child.type]);
    }
  }

  for (i = 0; scripts[i]; i++) {
    script = scripts[i];
    if (script[0].parentNode) {
	script[0].parentNode.removeChild(script[0]);
    }
    evalScript(script);
  }
};
