<?php

/*

Template Name: Partners

*/

?>



<?php get_header(); ?>

    <div id="text-content">
        <?php if ( have_posts() ) while ( have_posts() ) : the_post(); ?>
            <div id="content-title">
                <div class="caption"><h1><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h1></div>
                <div class="sub-caption">This is sub-caption</div>
                <div class="button">
                    <a href="#become">Become a Ikube partner</a>
                </div>
            </div>
            <div class="content column">
<!-- PAGE CONTENT -->

<!--                <div id="partners">-->
<!--                    <div class="caption">Types of partners</div>-->
<!--                    <div class="row first">-->
<!--                        <ul>-->
<!--                            <li>-->
<!--                                <div class="caption">Type of partnership</div>-->
<!--                                <div class="text">Proin gravida nibh vel velit auctor aliquet. Aenean sollicitudin, lorem quis bibendum auctor, nisi elit conse.</div>-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <div class="caption">Type of partnership</div>-->
<!--                                <div class="text">Proin gravida nibh vel velit auctor aliquet. Aenean sollicitudin, lorem quis bibendum auctor, nisi elit conse.</div>-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <div class="caption">Type of partnership</div>-->
<!--                                <div class="text">Proin gravida nibh vel velit auctor aliquet. Aenean sollicitudin, lorem quis bibendum auctor, nisi elit conse.</div>-->
<!--                            </li>-->
<!--                        </ul>-->
<!--                    </div>-->
<!---->
<!--                    <div class="row second">-->
<!--                        <div class="caption">Some of our Customers</div>-->
<!--                        <ul>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer1.png" alt="">-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer2.png" alt="">-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer3.png" alt="">-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer4.png" alt="">-->
<!--                            </li>-->
<!--                        </ul>-->
<!--                        <ul>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer5.png" alt="">-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer6.png" alt="">-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer7.png" alt="">-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/customer8.png" alt="">-->
<!--                            </li>-->
<!--                        </ul>-->
<!--                    </div>-->
<!---->
<!--                    <div class="row third">-->
<!--                        <div class="caption">How to Become a Partner</div>-->
<!--                        <div class="sub-caption">If your company is interested in becoming a Ikube, please fill in our application form below:</div>-->
<!--                        <div class="form">--><?php //echo do_shortcode( '[contact-form-7 id="40" title="Main Contact Form"]' ); ?><!--</div>-->
<!--                    </div>-->
<!--                </div>-->










                <?php
                the_content();
                ?>



<!-- END PAGE CONTENT -->

            </div>
        <?php endwhile; ?>
    </div>


<?php get_footer(); ?>