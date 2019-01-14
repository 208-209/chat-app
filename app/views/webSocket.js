'use strict';

import $ from 'jquery';

const $messages = $('#messages');
const webSocketUrl = $messages.data('url');

console.log(`webSocketUrl: ${webSocketUrl}`);

if (webSocketUrl) {

    const connection = new WebSocket(webSocketUrl);
    const $sendBtn = $("#message-send-button");

    $sendBtn.prop("disabled", true);

    connection.onopen = () => {
        $sendBtn.prop("disabled", false);

        // メッセージの送信
        $sendBtn.click(() => {
            const $meg = $("#meg");
            const text = $meg.val();
            const msg = {"message": text};

            console.log("msg : " + msg);

            $meg.val('');
            connection.send(JSON.stringify(msg))

        });

        // メッセージの削除
        $('.message-del-button').each((i, e) => {
           const button = $(e);
           button.click(() => {
               const deleteId = button.data('message-id');

               console.log("deleteId : " + deleteId)

           });
        });



    };

    connection.onclose = () => {
        $sendBtn.prop("disabled", true);
    };


    connection.onerror = function(error) {
        console.log('WebSocket Error ', error);
    };

    connection.onmessage = event => {
        const jsonData = JSON.parse(event.data);

        console.log(jsonData);
        


        if (jsonData.members) {
            /*
            const membersHtml = jsonData.members.split(',').map(member => `<li class="list-group-item">${member}</li>`).join('\n');
            $members.html(membersHtml);
             */

            $('.members').removeClass('isLogin');
            jsonData.members.split(',').map(member => {
                $(`#${member}`).addClass('isLogin')
            });
        }

        if (jsonData.message) {

            const messageId = jsonData.messageId;
            const message = jsonData.message;
            const updatedAt = jsonData.updatedAt;
            const userName = jsonData.userName;

            const hrEle = $('<hr>').attr({ class: 'message-hr', 'data-date': updatedAt});
            /*
            const iEle = $('<i>').attr({
                class: 'fas fa-trash-alt deleteBtn float-right message-del-button',
                'data-message-id': messageId,
                'data-placement': 'bottom',
                'title': 'このメッセージを削除する場合は、再読込してください'
            });
            */
            const strongEle = $('<strong>').text(userName);
            const divEle = $('<div>').attr({ class: 'balloon'}).text(message);

            $('<div>').attr({ id: messageId }).append(hrEle, strongEle, divEle).appendTo($messages);

            window.scrollTo({
                top: $(document).height(),
                behavior: "smooth"
            });

        }
    };

}


