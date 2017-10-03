$(function () {
    $(".extra").click(function () {
            var str = $(this).attr('data');
            $('<pre></pre>').dialog({
                modal: true,
                title: "Extra Info",
                open: function () {
                    $(this).html(str);
                },
                buttons: {
                    Ok: function () {
                        $(this).dialog("close");
                    }
                },
                width:'1000px',
                position: {
                    at: 'top',
                },
            });
        });

    $(".toggle_dark").click(function() {
        var image_wrapper = $(this).siblings(".img-wrapper").toggleClass("dark");
    })
});
