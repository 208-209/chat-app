'use strict';

import $ from 'jquery';
import bootstrap from 'bootstrap';

$('.entry-form').click(() => {
    const check = $('#entryRadioBtn [name=isPublic]:checked').val();

    if(check === 'true') {
        $('#entryCheckBox').hide();
    } else {
        $('#entryCheckBox').show();
    }
});

$('.edit-form').click(() => {
    const check = $('#editRadioBtn [name=isPublic]:checked').val();

    if(check === 'true') {
        $('#editCheckBox').hide();
    } else {
        $('#editCheckBox').show();
    }
});

window.scrollTo({
    top: $(document).height(),
    behavior: "smooth"
});
