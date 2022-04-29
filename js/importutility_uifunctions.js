/**
 * 
 */

function flowDown(wleft, wtop) {
            var w = jq('$win'), b = jq('$orderBtn');
            //Minimize from the Window to the order button
            jq('<div class="minimize" />').appendTo("body").css({
                left : wleft,
                top : wtop,
                width : w.width(),
                height : 10
            });
            p = b.offset();
            jq('.minimize').animate({
                left : p.left + b.width() / 2,
                top : p.top + b.height() / 2,
                width : 0,
                height : 0
            }, function() {
                jq(this).remove();
            });
        }