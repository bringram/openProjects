import Hello from './jsx/hello.jsx'; //es6 import!
import World from './jsx/world.jsx';

//import HelloWorld from './jsx/hello-world.jsx';

const PI = 3.141593;

document.getElementById('content').innerHTML = require("./content.js") + "<br/>The ES6 constant was hopefully rewritten to es5.  PI="+PI;

//require("./jsx/world.jsx"); //normal webpack import