'use strict';

import $ from 'jquery';

// node_modules/.bin/webpack --config conf/webpack.config.js

const $messages = $("#messages");
const $meg = $("#meg");
const $btn = $("#btn");


const channelId = $btn.data('channel-id');
const userId = $btn.data('user-id');

console.log(`channelId: ${channelId}, userId: ${userId}`);


const connection = new WebSocket(`ws://localhost:9000/channels/${channelId}/users/${userId}/message`);

$messages.before("<p>foo</p>");

$btn.prop("disabled", true);

connection.onopen = () => {
    $btn.prop("disabled", false);
    $btn.click(() => {
        const text = $meg.val();
        console.log(text);
        $meg.val('');
        connection.send(text)

    });
};

connection.onclose = () => {
    $btn.prop("disabled", true);
};


connection.onerror = function(error) {
    console.log('WebSocket Error ', error);
};

connection.onmessage = event => {
    $messages.append($("<p>" + event.data + "</p>"))
};

