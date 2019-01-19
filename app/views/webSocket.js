'use strict';

import $ from 'jquery';

const $messages = $('#messages');
const webSocketUrl = $messages.data('url');
let timerId;

if (webSocketUrl) {
    const $meg = $('#meg');
    const $sendBtn = $('#message-send-button');
    const connection = new WebSocket(webSocketUrl);



    $sendBtn.prop("disabled", true);

    connection.onopen = () => {
        $sendBtn.prop("disabled", false);

        // ボタンのクリックでメッセージの送信
        $sendBtn.click(() => {
            const text = $meg.val();
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
               const deleteId = button.data('message-id');
               connection.send(JSON.stringify({ deleteId: deleteId }))
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

        // メッセージ
        if (result.message) {
            $messages.append(createMessage(result));
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

    connection.onerror = function(error) {
        console.log('WebSocket Error ', error);
    };

}

/**
 *
 * @param result
 * @returns {*|jQuery|*|*|*|*}
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

    /*
    const iEle = $('<i>').attr({
        class: 'fas fa-trash-alt deleteBtn float-right message-del-button',
        'data-message-id': messageId,
        'data-placement': 'bottom',
        'title': 'このメッセージを削除する場合は、再読込してください'
    });
    */
}

/**
 * Herokuの設定で55秒間アイドルが続くと接続が閉じられるので、
 * 45秒間隔でダミーデータを送信し、接続を維持する（不本意）
 * @param connection
 */
function sendDummyData(connection) {
    timerId = setTimeout(() => {
        connection.send(JSON.stringify({ dummy: new Date()}));
        sendDummyData(connection)
    }, 45000)
}




