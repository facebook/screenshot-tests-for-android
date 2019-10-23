/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
