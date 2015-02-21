// Handler when DOM is ready
jQuery(document).ready(function()
    {

//        Main slider CUSTOMIZE FOR YOUR NEEDS

        jQuery('.main-bxslider').bxSlider({
//                auto: true,
            pagerCustom: '#bx-pager'
        });




        jQuery("#slider #bx-pager ul li a").click(function(){
            jQuery(this).addClass("active");
        });


        //        MOBILE MENU POPUP

        jQuery("#menu-mobile").hide();

        jQuery('.icon-show').show();

        jQuery("#menu-toggle").click(function () {
            jQuery("#menu-mobile").slideToggle(500);
            jQuery("header i").toggleClass("fa-times fa-bars");
        });






//        HEADER SCROLL MENU



        var lastScrollTop = 0;
        $(window).scroll(function(event){
            var st = $(this).scrollTop();
            if (st > lastScrollTop){
                if(st > 80) {
                    jQuery('body').addClass("header-scroll-down").removeClass("header-scroll-up"); // down
                    jQuery('header #menu-mobile').slideUp(0);
                    jQuery('header i').removeClass("fa-times");
                    jQuery('header i').addClass("fa-bars");
                }
                } else {
                    jQuery('body').removeClass("header-scroll-down").addClass("header-scroll-up"); // up
                    jQuery('header #menu-mobile').slideUp(200);
                    jQuery('header i').removeClass("fa-times");
                    jQuery('header i').addClass("fa-bars");
                }

            lastScrollTop = st;

            if (jQuery(this).scrollTop() <= 0) {
               jQuery('body').removeClass("header-scroll-up");
            }



        });


        // Feature COLUMN slider


        jQuery("#feature-col ul li .caption").addClass( "active-feature-col" );

        function tickSlideChange(caption){
            $('#feature-col ul li .caption').removeClass("active-feature-col");
            caption.addClass('active-feature-col');
            $('#feature-col .col-img img').attr('src', '/wp-content/themes/ikube-rwwp-theme-child/images/ikube-'+ caption.attr('id') +'.png')
        }

        function highlight(items, index) {
            index = index % items.length;
            tickSlideChange(items.eq(index));
            window.atvIkubeIntervalHandler = setTimeout(function() {
                highlight(items, index + 1)
            }, 3000);
        }

        highlight($('#feature-col ul li .caption'), 0);

        $('#feature-col ul li .caption').on('click', function(){
            clearTimeout(window.atvIkubeIntervalHandler);
            tickSlideChange($(this));
        });


//        VIDEO POPUP

        $(".fancybox").fancybox();

        $(".various").fancybox({
            padding     : 5,
            maxWidth	: 800,
            maxHeight	: 600,
            fitToView	: false,
            width		: '90%',
            height		: '90%',
            autoSize	: false,
            closeClick	: false,
            openEffect	: 'none',
            closeEffect	: 'none'

        });













//        Slider feature mobile

        jQuery('.bx-slider-480').bxSlider({
            mode: 'fade',
            pagerCustom: '#bx-pager-480'
        });


//        End slider feature mobile






//        FOOTER - MObile

            jQuery("footer .foot-col1 .caption").click(function () {
                jQuery(".foot-col1 ul").slideToggle(0);
            });

            jQuery("footer .foot-col2 .caption").click(function () {
                jQuery(".foot-col2 ul").slideToggle(0);
            });

            jQuery("footer .foot-col3 .caption").click(function () {
                jQuery(".foot-col3 ul").slideToggle(0);
            });

            jQuery("footer .foot-col4 .caption").click(function () {
                jQuery(".foot-col4 ul").slideToggle(0);
            });

            jQuery("footer .foot-col5 .caption").click(function () {
                jQuery(".foot-col5 ul").slideToggle(0);
            });


        if (jQuery(window).width() >= 767) {
            jQuery("footer .footer-column ul").show();
        } else {
            jQuery("footer .footer-column ul").hide();
        }

        jQuery(window).resize(function() {
            if (jQuery(window).width() >= 768) {
                jQuery("footer .footer-column ul").show();
            } else {
                jQuery("footer .footer-column ul").hide();
            }
        });

        
        // SERVICE PAGE

        jQuery("#services ul li").click(function(){
            jQuery("#services ul li.services-active").removeClass('services-active');
            jQuery(this).addClass('services-active');
        });


        var bodyHeight = $("body").height();
        var vwptHeight = $(window).height();
        if (vwptHeight > bodyHeight) {
            $("footer#colophon").css("position","absolute").css("bottom",0);
        }



        // CONTACT PAGE


//        buttonsError= function() {
//             jQuery("#contact button").click(function(){
//                 if (jQuery("#contact form").hasClass("invalid")) {
//                     jQuery("#contact button").addClass('btn-error');
//                 };
//             });
//
//             setInterval(function(){
//                 jQuery("#contact button").removeClass('btn-error');
//             }, 3500);
//
//        }
//
//        buttonsSuccess= function() {
//             jQuery("#contact button").click(function(){
//                 if (jQuery("#contact form").hasClass("send")) {
//                     jQuery("#contact button").addClass('btn-success');
//                 };
//             });
//
//             setInterval(function(){
//                 jQuery("#contact button").removeClass('btn-success');
//             }, 3500);
//
//        }
//
//        buttonsSuccess();
//        buttonsError();







        // Create a Marker
        
//
//            var image = '/wp-content/themes/ikube-rwwp-theme-child/images/pointer.png';
//            var mapOptions = {
//                zoom: 13,
//                center: new google.maps.LatLng(50.842122, 4.383925),
//                 mapTypeId: google.maps.MapTypeId.ROADMAP
//            }
//            var map = new google.maps.Map(document.getElementById('map'), mapOptions);
//            var myPos = new google.maps.LatLng(50.842122, 4.383925);
//            var myMarker = new google.maps.Marker({position: myPos, map: map, icon: image });


        // '/wp-content/themes/ikube-rwwp-theme-child/images/pointer.png' MARKER IMAGE

        function initialize() {
            var myLatlng = new google.maps.LatLng(50.842122, 4.383925);
            var mapOptions = {
                zoom: 4,
                center: myLatlng
            }
            var map = new google.maps.Map(document.getElementById('map'), mapOptions);

            var marker = new google.maps.Marker({
                position: myLatlng,
                map: map,
                title: 'Hello World!'
            });
        }

        google.maps.event.addDomListener(window, 'load', initialize);



//        FREE VERISON

        jQuery('.hide-text').hide();

        jQuery(".faq ul li:nth-child(1)").click(function () {
            jQuery(".faq ul li:nth-child(1) .hide-text").slideToggle(100);
        });

        jQuery(".faq ul li:nth-child(2)").click(function () {
            jQuery(".faq ul li:nth-child(2) .hide-text").slideToggle(100);
        });

        jQuery(".faq ul li:nth-child(3)").click(function () {
            jQuery(".faq ul li:nth-child(3) .hide-text").slideToggle(100);
        });

        jQuery(".faq ul li:nth-child(4)").click(function () {
            jQuery(".faq ul li:nth-child(4) .hide-text").slideToggle(100);
        });

        jQuery(".faq ul li:nth-child(5)").click(function () {
            jQuery(".faq ul li:nth-child(5) .hide-text").slideToggle(100);
        });


    }

);

// Handler when document is fully loaded
jQuery(window).load(function()
    {

    }
);

