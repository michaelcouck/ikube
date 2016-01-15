<!doctype html>
<!--[if lt IE 7]>      <html <?php language_attributes(); ?> class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html <?php language_attributes(); ?> class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html <?php language_attributes(); ?> class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html <?php language_attributes(); ?> class="no-js"> <!--<![endif]-->
	<head>
		<meta charset="<?php bloginfo('charset'); ?>">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

        

		<title><?php wp_title( '|', true, 'right' ); ?></title>

        <link href="<?php echo get_stylesheet_directory_uri(); ?>/favicon.png" rel="shortcut icon">

        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link href='http://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800' rel='stylesheet' type='text/css'>

		<!--Add styles-->
		<link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/css/normalize.css">
		<link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/css/main.css">
		<link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/css/additional.css">

		<link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/css/responsive.css">
		<link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/font-awesome/css/font-awesome.css">



        <link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/css/jquery.bxslider.css">

 <!--        <link rel="stylesheet" href="<?php echo get_stylesheet_directory_uri(); ?>/css/component.css"> -->



		<script src="<?php echo get_stylesheet_directory_uri(); ?>/js/vendor/modernizr-2.6.2.min.js"></script>

        <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true"></script>




		<?php
			atv_init();
/*			$body_class = "is-home";
			if(!atv_is_home_page())
				$body_class = "other-page";*/
			wp_head();
		?>
	</head>
	<body <?php body_class(); ?>>
		<!--[if lt IE 7]>
		<p class="browsehappy">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> to improve your experience.</p>
		<![endif]-->

        <!--[if lt IE 8]>
        <p class="browsehappy">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> to improve your experience.</p>
        <![endif]-->

        <header>
            <div id="header-wrapper" class="column clearfix">
                <div id="header-left" class="clearfix">
                    <div id="logo">
                        <a href="/"><img src="<?php echo get_stylesheet_directory_uri(); ?>/images/logo.png" alt=""></a>
                    </div>
                    <nav id="languages">
                        <ul>
                            <li>
                                <a>Eng <i class="fa fa-caret-down"></i></a>
                                <ul class="submenu">
                                    <li>
                                        <a href="#">English</a>
                                    </li>
                                    <li>
                                        <a href="#">Nederlands</a>
                                    </li>
                                    <li>
                                        <a href="#">Русский</a>
                                    </li>
                                    <li>
                                        <a href="#">Dansk</a>
                                    </li>
                                    <li>
                                        <a href="#">Français</a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </nav>
                </div>
                <div id="header-right" class="clearfix">
                    <nav id="menu">
                        <ul>
                            <li>
                                <a href="/our-services/data-integration/">OUR SERVICES</a>
                            </li>
                            <li>
                                <a href="/solution/">SOLUTIONS</a>
                            </li>
                            <li>
                                <a href="/resources/">RESOURCES</a>
                            </li>
                            <li>
                                <a href="/partners/">PARTNERS</a>
                            </li>
                            <li>
                                <a href="/contact/">CONTACTS</a>
                            </li>
                        </ul>
                    </nav>
                        <!--<div id="menu-toggle">MENU <i class="fa fa-bars fa-lg icon-show"></i><i class="fa fa-times icon-close"></i></div>-->

                    <div id="menu-toggle">
                        MENU <i class="fa fa-lg fa-bars"></i>
                    </div>

                    <div id="download-ikube" class="button">
                        <a href="#">Download free</a>
                    </div>
                </div>
                <div class="clearfix"></div>
                <div id="menu-mobile">
                    <ul id="show-menu">
                        <li>
                            <a href="/our-services/">OUR SERVICES</a>
                        </li>
                        <li>
                            <a href="/solution/">SOLUTIONS</a>
                        </li>
                        <li>
                            <a href="/resources/">RESOURCES</a>
                        </li>
                        <li>
                            <a href="#">PARTNERS</a>
                        </li>
                        <li>
                            <a href="/contact/">CONTACTS</a>
                        </li>
                    </ul>
                    <div class="button">
                        <a href="#">Download free</a>
                    </div>
                </div>
            </div>
        </header>

        <main>
        <?php if(atv_is_home_page()): ?>
            <section id="banner">
                <div id="banner-wrapper" class="column">
                    <div class="caption">DATA & BUSINESS ANALYTICS SOFTWARE</div>
                    <div class="sub-caption">We provide software and services in the field of big data analytics, including predictive analytics, data mining and text mining. We making analytics results searchable and can process unprecedented volumes.</div>
                    <div id="download-link" class="button">
                        <a href="#">Download iKube for free</a>
                    </div>
                    <div id="video">
                        <ul class="list">
                            <li>
                                <a href="http://www.youtube.com/embed/L9szn1QQfas?autoplay=1" class="various fancybox.iframe">
                                    <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/video.png" alt="">
                                </a>
                            </li>
                        </ul>


                    </div>
                    <div id="video-mobile" class="mob-480">
                        <div class="text">
                           <a href="http://www.youtube.com/embed/L9szn1QQfas?autoplay=1"  class="various fancybox.iframe">Video about ikube <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/play.png" alt=""></a>
                        </div>
                    </div>
                </div>
            </section>
             <?php endif; ?> 
            <section id="wp-content">
            	<div id="wp-content-wrapper">

            		
