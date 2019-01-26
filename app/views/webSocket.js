'use strict';

import $ from 'jquery';

const $messages = $('#messages');
const webSocketUrl = $messages.data('url');
let timerId;

// WebSocketによるメッセージの送信と削除
if (webSocketUrl) {
    const $meg = $('#meg');
    const $sendBtn = $('#message-send-button');
    const connection = new WebSocket(webSocketUrl);

    $sendBtn.prop("disabled", true);

    connection.onopen = () => {
        $sendBtn.prop("disabled", false);

        // ボタンのクリックでメッセージの送信
        $sendBtn.click(() => {
            const text = $meg.val().slice(0, 255);
            $meg.val('');
            connection.send(JSON.stringify({ message: text }))
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
            result.members.split(',').map(member => {
                $(`#${member}`).addClass('isLogin')
            });
        }

        // メッセージの表示
        if (result.message) {
            $messages.append(createMessage(result));
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
 * メッセージのHTML要素を作成
 *
 * @param result
 * @returns メッセージのHTML要素
 */
function createMessage(result) {
    const messageId = result.messageId;
    const message = result.message;
    const updatedAt = result.updatedAt;
    const userName = result.userName;
    const profileImageUrl = result.profileImageUrl;

    const hrEle = $('<hr>').attr({ class: 'message-hr', 'data-date': updatedAt});
    const imgEle = $('<img>').attr({src: profileImageUrl, alt: 'profile-image', class: 'rounded mx-auto d-block'});
    const strongEle = $('<strong>').text(userName);
    const messageEle = $('<p>').addClass('message-area').text(message);

    const profileImageDiv = $('<div>').addClass('col-sm-2').append(imgEle);
    const messageDiv = $('<div>').addClass('col-sm-10').append(strongEle, messageEle);
    const messageAreaDiv = $('<div>').addClass('row').append(profileImageDiv, messageDiv);

    return $('<div>').attr({ id: messageId }).append(hrEle, messageAreaDiv);
}

/**
 * 45秒間隔でダミーデータ（日付）を送信する
 * Herokuの設定で55秒間アイドルが続くと接続が閉じられるので、
 * ダミーデータを送信し、接続を維持する（不本意）
 *
 * @param connection
 */
function sendDummyData(connection) {
    timerId = setTimeout(() => {
        connection.send(JSON.stringify({ dummy: new Date()}));
        sendDummyData(connection)
    }, 45000)
}




