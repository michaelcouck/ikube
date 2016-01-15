<?php

/*

Template Name: Free Version

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

<!--                <div id="free-version">-->
<!---->
<!--                    <div id="download-block">-->
<!--                        <div id="download-block-wrapper">-->
<!--                            <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/logo.png" alt="">-->
<!--                            <span class="first">-->
<!--                                Download Ikube v1.2-->
<!--                            </span>-->
<!--                            <div class="button">-->
<!--                                <a href="#">Start download</a>-->
<!--                            </div>-->
<!--                            <span class="second">-->
<!--                                Last Updated: 10.27.2014 14:14:18 EDT-->
<!--                            </span>-->
<!--                            <ul>-->
<!--                                <li><a href="#">Release Notes</a></li>-->
<!--                                <li>|</li>-->
<!--                                <li><a href="#">Documentation</a></li>-->
<!--                            </ul>-->
<!--                        </div>-->
<!--                    </div>-->
<!---->
<!--                    <div class="caption first">About Ikube</div>-->
<!--                    <div class="text">Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 15. Lorem Ipsum is simply dummy text of the.<br><br>-->
<!--                        Ikube is very good solutions in diferent sphere of work. If would like to customize Ikube for your needs, contact us and we will help you with it-->
<!--                    </div>-->
<!--                    <div class="button">-->
<!--                        <a href="#">Contact us</a>-->
<!--                    </div>-->
<!---->
<!--                    <div class="caption second">System Requirements</div>-->
<!--                    <div class="text">-->
<!--                        We officially support Linux. But it runs on any operating system, Java based, PC and Mac. It will run anywhere, even on Andriod if it have enough memory. For big configurations give it 16 gb of memory, but it can run with 256 mb.-->
<!--                    </div>-->
<!--                    <div class="image">-->
<!--                        <img src="--><?php //echo get_stylesheet_directory_uri(); ?><!--/images/system.png" alt="">-->
<!--                    </div>-->
<!---->
<!--                    <div class="caption">FAQ</div>-->
<!--                    <div class="faq">-->
<!--                        <ul>-->
<!--                            <li>-->
<!--                                <b>Aenean sollicitudin, lorem quis bibendu?</b>-->
<!--                                <div class="hide-text">-->
<!--                                    test text test text test text test text-->
<!--                                </div>-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <b>Justo. Nullam ac urna eu felis dapibus condime dfd dntu?</b>-->
<!--                                <div class="hide-text">-->
<!--                                    test text test text test text test text-->
<!--                                </div>-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <b>Etiam pharetra, erat sed fermenturis e?</b>-->
<!--                                <div class="hide-text">-->
<!--                                    test text test text test text test text-->
<!--                                </div>-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <b>Etiam pharetra, erat sed fermentum feugiat, velit mauri?</b>-->
<!--                                <div class="hide-text">-->
<!--                                    test text test text test text test text-->
<!--                                </div>-->
<!--                            </li>-->
<!--                            <li>-->
<!--                                <b>Lorem Ipsum has been the industry's standard dummy text ever lorem sinc?</b>-->
<!--                                <div class="hide-text">-->
<!--                                    test text test text test text test text-->
<!--                                </div>-->
<!--                            </li>-->
<!--                        </ul>-->
<!--                    </div>-->
<!--                    <div class="button">-->
<!--                        <a href="#">Ask a question</a>-->
<!--                    </div>-->
<!---->
<!---->
<!---->
<!--                </div>-->







                <?php
                the_content();
                ?>

<!-- END PAGE CONTENT -->            	

            </div>




        <?php endwhile; ?>
    </div>


<?php get_footer(); ?>