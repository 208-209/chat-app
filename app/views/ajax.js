'use strict';

import $ from 'jquery';

// AJAXによるBookmarkの更新
$('.bookmark-toggle-button').each((i, e) => {
    const button = $(e);
    button.click(() => {
        const thisChannelId = $('#this-channel').data('channel-id');
        const channelId = button.data('channel-id');
        const userId = button.data('user-id');
        const bookmark = button.data('bookmark');
        const CSRF_TOKEN = $('input[name="csrfToken"]').attr('value');

        $.ajax({
            type: "POST",
            url: `/channels/${channelId}/users/${userId}/bookmark`,
            data: { "bookmark": bookmark },
            beforeSend: function(xhr) {
                xhr.setRequestHeader("Csrf-Token", CSRF_TOKEN);
            },
            success: function (data) {
                // アイコンを変化
                button.data('bookmark', data.bookmark);
                button.removeClass('fas', 'far');
                const className = data.bookmark ? 'fas' : 'far';
                button.addClass(className);

                // ブックマークエリアに要素の追加と削除
                if(data.bookmark) {
                    const tag = channelId === thisChannelId ? $('<span>') : $('<a>').attr({ href: `/channels/${channelId}` });
                    $('<li>').attr({ id: channelId, class: 'list-group-item ellipsis' }).append(tag.text(data.channelName)).appendTo('#bookmark');
                } else {
                    $(`#${channelId}`).remove()
                }
            }
        });
    });
});


