'use strict';

import $ from 'jquery';

const $messages = $('#messages');
const webSocketUrl = $messages.data('url');
let timerId;

// WebSocketによるメッセージの送信と削除
if (webSocketUrl) {
    const $meg = $('#meg');
    const $sendBtn = $('#message-send-button');
    const userId = $sendBtn.data('user-id');
    const connection = new WebSocket(webSocketUrl);

    $sendBtn.prop("disabled", true);

    connection.onopen = () => {
        $sendBtn.prop("disabled", false);

        // ボタンのクリックでメッセージの送信
        $sendBtn.click(() => {
            const text = $meg.val().slice(0, 255);
            if (text) {
                connection.send(JSON.stringify({ message: text }))
                $meg.val('');
            }
        });

        // Enterでメッセージの送信
        $meg.keypress(event => {
            const keycode = event.keyCode ? event.keyCode : event.which;
            if (keycode === 13) $sendBtn.click()
        });

        // メッセージの削除
        $('.message-del-button').each((i, e) => {
            const button = $(e);
            button.click(() => {
                const messageId = button.data('message-id');
                connection.send(JSON.stringify({ delete: messageId }))
            });
        });

        // ダミーデータの送信
        sendDummyData(connection)
    };


    connection.onclose = () => {
        $sendBtn.prop("disabled", true);
        clearTimeout(timerId);
    };


    connection.onmessage = event => {
        const result = JSON.parse(event.data);

        // ログインメンバー情報
        if (result.members) {
            $('.members').removeClass('isLogin');
            result.members.forEach(member => {
                $(`#${member}`).addClass('isLogin')
            });
        }

        // メッセージの表示
        if (result.message) {
            $messages.append(createMessage(result, connection, userId));
            window.scrollTo({
                top: $(document).height(),
                behavior: "smooth"
            });
        }

        // メッセージの削除
        if (result.delete) {
            $(`#${result.delete}`).remove()
        }
    };

    connection.onerror = function(error) {
        console.log('WebSocket Error ', error);
    };
}

/**
 * クライアント側から表示するメッセージのHTMLを作成する
 * @param result サーバーから送られてきたメッセージデータのJSON
 * @param connection WebSocket
 * @param userId アクセスユーザーのID
 * @returns {*} メッセージのHTML
 */
function createMessage(result, connection, userId) {
    const messageId = result.messageId;
    const message = result.message;
    const createdBy = result.createdBy;
    const userName = result.userName;
    const profileImageUrl = result.profileImageUrl;
    const updatedAt = result.updatedAt;

    const horizontalRule = $('<hr>').attr({ class: 'message-hr', 'data-date': updatedAt});
    const trashBtn = $('<i>').attr({
        id: messageId,
        class: 'fas fa-trash-alt deleteBtn float-right message-del-button',
        'data-message-id': messageId,
        'title': 'このメッセージを削除します'
    }).click(() => {
        connection.send(JSON.stringify({ delete: messageId }))
    });
    const profileImage = $('<img>').attr({src: profileImageUrl, alt: 'profile-image', class: 'rounded mx-auto d-block'});
    const userNameEle = $('<strong>').text(userName);
    const messageEle = $('<p>').addClass('message-area').text(message);

    const profileImageDiv = $('<div>').addClass('col-sm-2').append(profileImage);
    const messageDiv = $('<div>').addClass('col-sm-10').append(userNameEle, messageEle);
    const messageAreaDiv = $('<div>').addClass('row').append(profileImageDiv, messageDiv);

    // アクセスユーザーとメッセージの作成者が同一ならば、削除ボタンも表示される
    return userId === createdBy ? $('<div>').attr({ id: messageId }).append(horizontalRule, trashBtn, messageAreaDiv) : $('<div>').attr({ id: messageId }).append(horizontalRule, messageAreaDiv);
}

/**
 * 45秒間隔でダミーデータ（日付）を送信する
 * Herokuの設定で55秒間アイドルが続くと接続が閉じられるので、
 * ダミーデータを送信し、接続を維持する
 * @param connection
 */
function sendDummyData(connection) {
    timerId = setTimeout(() => {
        connection.send(JSON.stringify({ dummy: new Date()}));
        sendDummyData(connection)
    }, 45000)
}
