'use strict';

import $ from 'jquery';

// AJAXによるBookmarkの更新
$('.bookmark-toggle-button').each((i, e) => {
    const button = $(e);
    button.click(() => {
        const channelId = button.data('channel-id');
        const channelName = button.data('channel-name');
        const userId = button.data('user-id');
        const bookmark = button.data('bookmark');
        const CSRF_TOKEN = $('input[name="csrfToken"]').attr('value');

        console.log(`channelId: ${channelId}, userId: ${userId}, bookmark: ${bookmark}`);

        $.ajax({
            type: "POST",
            url: `/channels/${channelId}/users/${userId}/bookmark`,
            data: { "bookmark": bookmark },
            beforeSend: function(xhr) {
                xhr.setRequestHeader("Csrf-Token", CSRF_TOKEN);
            },
            success: function (data) {
                const jsonData = JSON.parse(data);

                // アイコン
                button.data('bookmark', jsonData.bookmark);
                button.removeClass('fas', 'far');
                const className = jsonData.bookmark === 'true' ? 'fas' : 'far';
                button.addClass(className);

                // ブックマークエリアに要素の追加と削除
                if(jsonData.bookmark === 'true') {
                    const anchor = $('<a>').attr({ href: `/channels/${channelId}` }).text(channelName);
                    $('<li>').attr({ id: channelId, class: 'list-group-item' }).append(anchor).appendTo('#bookmark');
                } else {
                    $(`#${channelId}`).remove()
                }
            }
        });



        /*
        $.post(`/users/${userId}/games/${gameId}/favorite`,
            { favorite: nextFavorite },
            (data) => {
                button.data('favorite', data.favorite);
                const buttonStyles = ['fa-star-o', 'fa-star'];
                button.removeClass('fa-star-o', 'fa-star');
                button.addClass(buttonStyles[data.favorite]);
            });

        */
    });
});

/*
const editBtn = $('#edit-button');
editBtn.click(() => {
    const channelId = editBtn.data('channel-id');
    const channelName = $('#channelName-form').val();
    const description = $('#description-form').val();
    const CSRF_TOKEN = $('input[name="csrfToken"]').attr('value');
    const editData = {
        "channelName": channelName,
        "description": description
    };

    if (channelName && description) {
        $.ajax({
            type: "POST",
            url: `/channels/${channelId}/update`,
            data: editData,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Csrf-Token', CSRF_TOKEN);
            },
            success: function (data) {
                console.log(data);

                $('#channel-channelName').text(data.channelName);
                $('#channel-description').text(data.description);
                $('#channel-updatedAt').text(data.updatedAt);
            }
        })
    }
});
*/