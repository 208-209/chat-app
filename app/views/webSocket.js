'use strict';

import $ from 'jquery';

const $messages = $('#messages');
const webSocketUrl = $messages.data('url');


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
            $meg.val('');
            connection.send(JSON.stringify({ message: text }))
        });

        // メッセージの削除
        $('.message-del-button').each((i, e) => {
           const button = $(e);
           button.click(() => {
               const deleteId = button.data('message-id');
               connection.send(JSON.stringify({ deleteId: deleteId }))
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
        const result = JSON.parse(event.data);

        // ログインメンバー情報
        if (result.members) {
            $('.members').removeClass('isLogin');
            result.members.split(',').map(member => {
                $(`#${member}`).addClass('isLogin')
            });
        }

        // メッセージ
        if (result.message) {
            const messageId = result.messageId;
            const message = result.message;
            const updatedAt = result.updatedAt;
            const userName = result.userName;
            const profileImageUrl = result.profileImageUrl;

            console.log(profileImageUrl);

            const hrEle = $('<hr>').attr({ class: 'message-hr', 'data-date': updatedAt});
            const col2div = $('<div>').addClass('col-sm-2');
            const col10div = $('<div>').addClass('col-sm-10');




            const imgEle = $('<img>').attr({src: profileImageUrl, alt: 'profile-image', class: 'rounded mx-auto d-block'});

            /*
            const iEle = $('<i>').attr({
                class: 'fas fa-trash-alt deleteBtn float-right message-del-button',
                'data-message-id': messageId,
                'data-placement': 'bottom',
                'title': 'このメッセージを削除する場合は、再読込してください'
            });
            */
            const strongEle = $('<strong>').text(userName);
            const messageEle = $('<p>').addClass('message-area').text(message);


            const imgArea = col2div.append(imgEle);

            const messageArea =col10div.append(strongEle, messageEle);


            const divElement = $('<div>').addClass('row').append(imgArea, messageArea);

            $('<div>').attr({ id: messageId }).append(hrEle, divElement).appendTo($messages);

            window.scrollTo({
                top: $(document).height(),
                behavior: "smooth"
            });

        }

        // 削除
        if (result.deleteId) {
            $(`#${result.deleteId}`).remove()
        }
    };

}


