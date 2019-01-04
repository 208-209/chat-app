'use strict';

import $ from 'jquery';

// node_modules/.bin/webpack --config conf/webpack.config.js

const $members = $("#members");
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
        const msg = {"message": text};
        console.log(text);
        $meg.val('');
        connection.send(JSON.stringify(msg))

    });
};

connection.onclose = () => {
    $btn.prop("disabled", true);
};


connection.onerror = function(error) {
    console.log('WebSocket Error ', error);
};

connection.onmessage = event => {
    console.log(event.data);
    const jsonData = JSON.parse(event.data);
    console.log(typeof jsonData);
    console.log(jsonData.msg);
    console.log(jsonData.members);

    if (jsonData.members) {
        const membersHtml = jsonData.members.split(',').map(m => "<p>" + m + "</p>").join('\n');
        $members.html("<div>" + membersHtml + "</div>");
    }

    if (jsonData.message) {
        $messages.append($("<p>" + jsonData.message + "</p>"))
    }
};

