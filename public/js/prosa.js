/*====================================
 =            ON DOM READY            =
 ====================================*/
$(function() {
    $('.toggle-nav').click(function() {
        toggleNav();
    });
});


/*========================================
 =          TOGGLE MENU           =
 ========================================*/
function toggleNav() {
    if ($('#site-wrapper').hasClass('show-nav')) {
        // Do things on Nav Close
        $('#site-wrapper').removeClass('show-nav');
        $('#toggle-menu').show();
    } else {
        // Do things on Nav Open
        $('#site-wrapper').addClass('show-nav');
        $('#toggle-menu').hide();
    }
}

// ESC key close nav bar
$(document).keyup(function(e) {
    if (e.keyCode === 27) {
        if ($('#site-wrapper').hasClass('show-nav')) {
            toggleNav();
        }
    }
});


/* exported ImageLoader */
var ImageLoader = (function($) {

    'use strict';


    var module = {
        done: function(e) {
            e.addClass('image-loaded');
        },
        success: function(e, image) {
            e.css('background-image', 'url(' + image.src + ')');
            module.done(e);
        },
        init: function($el) {
            var image = new Image(),
                src = $el.data('load-image');

            if(src.length > 0) {
                image.src = src;
                image.onload = module.success($el, image);
                image.onerror = module.done($el);
                image.onabort = module.done($el);
            }

            return image;
        }
    };

    return { load: module.init };

}(jQuery));