'use strict';

// node_modules/.bin/webpack --config conf/webpack.config.js

import $ from 'jquery';
const global = Function('return this;')();
global.jQuery = $;
import bootstrap from 'bootstrap';