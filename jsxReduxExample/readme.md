#About Project
This project is an example of configuring a simple spring boot thymleaf with jsx, babel, webpack front end build.

Assuming you have npm setup and working.

#Steps to Building
## I am starting with settting up webpack
https://webpack.github.io/docs/tutorials/getting-started/
```
$>npm install webpack -g
```

## Simple example
I just added a basic *webpack.config.js* which allows me to run `$>webpack` to make the build.  Basically
webpack just bundles the described files together into a "bundle".  Then it is smart enough to bundle things together
by parsing the javascript.  

Unfortunately when I look at the compiled *bundle.js* it is not really doing anything very *smart*.  Its actually stupid.  All
it did was merge my files and add scope them.  It does not do what require.js does or any actualy dynamic dependency loading.
What is the point???

Further I looked into this, all be it only a little, and found this: https://github.com/petehunt/webpack-howto#9-async-loading

But seriously, I want to have my router manage my dependencies?  Where is the encapsulation there.  Again, stupid!  I think
I will stick with compiling our components seperatly and using a require.js style of loading from the modules themselves.

This transpilation into some incomprehensible js file seems silly.  I have avoided require.js, but I think its better than
this.  I think using webpack to make base bundles is probably pretty good.

## Install Babel
```
$>npm install babel-loader babel-core babel-preset-es2015 webpack --save-dev
```

## Install Webpack React
```
$>npm install babel-preset-react --save-dev
$>npm install react --save
$>npm install react-dom --save
```


## Change the webpack.config.js
I modified the example.js file to use ES6 "const" keyword.  You can see in the bundle.js, this was modified to be ES5 compliant. Issue
here is "const" and "var" really mean very different things, and with hoisting, these may act very different.
```
module.exports = {
    entry: "./ui/js/example.js",
    output: {
        path: __dirname,
        filename: "./public/dist/js/bundle.js"
    },
    module: {
        loaders: [
            /*{ test: /\.css$/, loader: "style!css" }*/
            {
              test: /\.js$/,
              exclude: /(node_modules|bower_components)/,
              loader: 'babel-loader',
              query: {
                presets: ['es2015', 'react']
              }
            }
          ]
    }
};
```

##React seems bulky, lets use preact!
Once switching to preact, the bundle.js was 64 KB, which was 727 KB from react.  I mean thats a 10x improvement!

```
$>npm install preact-compat --save
$>npm install preact --save
```

Simply alias the react and react-dom
```
    ...
    resolve: {
        alias: {
            'react': 'preact-compat',
            'react-dom': 'preact-compat'
        }
    }
    ...
```