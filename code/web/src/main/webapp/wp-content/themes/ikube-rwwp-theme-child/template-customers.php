<?php

/*

Template Name: Customers
*/

?>



<?php get_header(); ?>

    <div id="text-content">
        <?php if ( have_posts() ) while ( have_posts() ) : the_post(); ?>
            <div id="content-title">
                <div class="caption"><h1><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h1></div>
                <div class="sub-caption">This is sub-caption</div>
            </div>
            <div class="content column">
<!-- PAGE CONTENT -->

                <!--

                <div id="customers-reviews">
                    <div class="row first">
                        <div class="customers-type">
                            <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/economie.png" alt="">
                        </div>
                        <div class="caption">Ministry of Economics Belgium</div>
                        <div class="sub-caption">FPS Economy, SMEs, Self-Employed and Energy.</div>
                        <div class="text">The Directorate General Statistics and Economic Information (DGSEI) is in charge of the national (official) statistics in Belgium. The DGSEI is also responsible for the production of European statistics. These pages contain information on the organisation of the DGSEI, contact details and a brief history.</div>
                        <div class="review arrow_box">
                            <span class="clearfix">
                            <div class="photo"><img src="<?php echo get_stylesheet_directory_uri(); ?>/images/photo1.png" alt=""></div>
                            <div class="phrase">Donec leo metus, fermentum vitae arcu vel, tempus finibus tortor.  Aliquam gravida ornare mauris, a mattis elit aliquam vitae</div>
                            <div class="author">Jon Farell,  Jobposition in Ministry</div>
                                </span>
                        </div>
                    </div>
                    <div class="row second">
                        <div class="customers-type">
                            <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/bosal.png" alt="">
                        </div>
                        <div class="caption">BOSAL</div>
                        <div class="sub-caption">Leading worldwide manufacturer of OE</div>
                        <div class="text">Design, develop, and manufacture original equipment exhaust systems for carmakers such as GM and Ford. We supply OE service products to many import / transplant carmakers including VW, Honda, Volvo, Subaru, and Mazda. We also develop high performance exhaust for OEM port of entry and dealer programs</div>
                        <div class="review arrow_box">
                            <span class="clearfix">
                            <div class="photo"><img src="<?php echo get_stylesheet_directory_uri(); ?>/images/photo2.png" alt=""></div>
                            <div class="phrase">Donec leo metus, fermentum vitae arcu vel, tempus finibus tortor.  Aliquam gravida ornare mauris, a mattis elit aliquam vitae</div>
                            <div class="author">Jon Farell,  Jobposition in Ministry</div>
                                </span>
                        </div>
                    </div>

                    <div class="row third">
                        <div class="caption">Some of our Customers</div>
                        <ul>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer1.png" alt="">
                            </li>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer2.png" alt="">
                            </li>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer3.png" alt="">
                            </li>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer4.png" alt="">
                            </li>
                        </ul>
                        <ul>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer5.png" alt="">
                            </li>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer6.png" alt="">
                            </li>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer7.png" alt="">
                            </li>
                            <li>
                                <img src="<?php echo get_stylesheet_directory_uri(); ?>/images/customer8.png" alt="">
                            </li>
                        </ul>
                    </div>

                    <div class="row fourth">
                        <div class="caption last">Bring the power of ikube to your Business</div>
                        <div id="choice">
                            <ul>
                                <li>
                                    <div class="button"><a href="#">Download iKube for free</a></div></li>
                                <li>or</li>
                                <li>
                                    <div class="button"><a href="#">Contact us</a></div>
                                </li>
                            </ul>
                    </div>

                </div>


                    -->



                    <?php the_content();?>

<!-- END PAGE CONTENT -->            	

            </div>




        <?php endwhile; ?>
    </div>


<?php get_footer(); ?>