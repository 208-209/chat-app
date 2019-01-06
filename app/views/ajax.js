'use strict';

import $ from 'jquery';

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