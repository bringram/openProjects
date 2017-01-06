import React from 'react';
import ReactDOM from 'react-dom';

import Hello from './hello.jsx';
import World from './world.jsx';


class HelloWorld extends React.Component {
  render() {
    return <div><Hello/><World/></div>
  }
}

//make this available externally
module.exports = HelloWorld;
