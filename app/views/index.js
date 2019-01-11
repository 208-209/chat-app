'use strict';

// node_modules/.bin/webpack --config conf/webpack.config.js

import $ from 'jquery';
const global = Function('return this;')();
global.jQuery = $;
import bootstrap from 'bootstrap';



$(function () {
    $('[data-toggle="tooltip"]').tooltip();
});


$('.entry-form').click(() => {
    const check = $('#entryRadioBtn [name=isPublic]:checked').val();
    console.log(check);

    if(check === 'true') {
        $('#entryCheckBox').hide();
    } else {
        $('#entryCheckBox').show();
    }
});


$('.edit-form').click(() => {
    const check = $('#editRadioBtn [name=isPublic]:checked').val();
    console.log(check);

    if(check === 'true') {
        $('#editCheckBox').hide();
    } else {
        $('#editCheckBox').show();
    }
});
