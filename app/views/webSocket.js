'use strict';

import $ from 'jquery';

const $members = $("#members");
const $messages = $("#messages");
const $meg = $("#meg");
const $btn = $("#btn");


const channelId = $btn.data('channel-id');
const userId = $btn.data('user-id');
const webSocketUrl = $btn.data('url');

console.log(`channelId: ${channelId}, userId: ${userId}, url: ${webSocketUrl}`);


const connection = new WebSocket(`${webSocketUrl}/users/${userId}/message`);

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
        const membersHtml = jsonData.members.split(',').map(member => `<li class="list-group-item">${member}</li>`).join('\n');
        $members.html(membersHtml);
    }

    if (jsonData.message) {
        $messages.append($("<p>" + jsonData.message + "</p>"))
    }
};