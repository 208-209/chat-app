const path = require('path');

module.exports = {
    entry: ['./app/views/index.js', './app/views/ajax.js', './app/views/webSocket.js'],
    mode: 'none',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, '../public/javascripts')
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                use: [
                    {
                        loader: 'babel-loader',
                        options: {
                            presets: [
                                ['@babel/preset-env', { 'modules': false }],
                                '@babel/preset-react'
                            ]
                        }
                    }
                ],
                // node_modules は除外する
                exclude: /node_modules/,
            }
        ]
    }
};