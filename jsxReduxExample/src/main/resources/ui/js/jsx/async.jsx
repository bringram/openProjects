import React from 'react';
import ReactDOM from 'react-dom';

//load the helloworld plugin
require.ensure(["./helloworld.jsx"],()=>{
    var HelloWorld = require('./helloworld.jsx');
    ReactDOM.render(<HelloWorld/>, document.getElementById('helloworld'));
});