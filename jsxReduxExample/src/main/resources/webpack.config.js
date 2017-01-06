var webpack = require("webpack");
var VendorChunkPlugin = require('webpack-vendor-chunk-plugin');
module.exports = {
    //entry point for application and statically created pages
    entry: {
        //this is a "true" entry point
        main : "./ui/js/example.js",

        //creates a pages we can load dynamically, these would be components
        async : "./ui/js/jsx/async.jsx",
        async2: "./ui/js/jsx/async2.jsx",

        //all the vendor stuff that we want imports to basically ignore.  See the pluins section for global.js
        global: ['react','react-dom']
    },
    output: {
        path: __dirname,
        filename: "./public/dist/js/[name].js", //these are the entry points
        chunkFilename: './public/dist/js/[id].chunk.js' //these are require.ensure() which cause dynamic loading...
        //plugins: [ new webpack.optimize.CommonsChunkPlugin("main.js") ]
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin('global', './public/dist/js/global.js'),
        new VendorChunkPlugin('global'), //extracts the vender ("global") stuff from our components
    ],
    module: {
        loaders: [
            /*{ test: /\.css$/, loader: "style!css" }*/
            {
              test: /\.jsx?$/,
              exclude: /(node_modules|bower_components)/,
              loader: 'babel-loader',
              query: {
                presets: ['es2015', 'react']
              }
            }
          ]
    }//,
    //remove this if you want to use react
    /*resolve: {
        alias: {
            'react': 'preact-compat',
            'react-dom': 'preact-compat'
        }
    }*/
};