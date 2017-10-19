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
        $(this).closest(".screenshot").find(".img-wrapper").toggleClass("dark");
    })

    $(".toggle_hierarchy").click(function() {
        $(this).closest(".screenshot").find(".hierarchy-overlay").toggle();
    })

    $(".view-hierarchy")
        .mousemove(
            function(e) {
                $(".hierarchy-node").removeClass('highlight');
                $($(e.target).closest("details").attr('target')).addClass('highlight');
            })
        .mouseout(
            function() {
                $(".hierarchy-node").removeClass('highlight');
            });
});
