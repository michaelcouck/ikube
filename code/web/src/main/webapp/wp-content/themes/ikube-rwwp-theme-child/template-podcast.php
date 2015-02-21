<?php

/*

Template Name: Podcast

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

<!--            <div id="podcast">-->
<!--                <ul>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                            odio sit amet nibhp psum, nec sagittis sem-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                            c sagittis sem-->
<!--                            nibh id elit is-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                            um, nec sagittis sem-->
<!--                            nibh id elit is-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                            odio sit amet-->
<!--                            nibh id elit is-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                            odio sit amet nibhp psum, nec sagittis sem-->
<!--                            nibh id elit is-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            nibh id elit is-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                        </div>-->
<!--                    </li>-->
<!--                    <li>-->
<!--                        <iframe width="320" height="180" src="https://www.youtube.com/embed/dRjE1JwdDLI?rel=0&autohide=0&showinfo=0" frameborder="0" allowfullscreen></iframe>-->
<!--                        <div class="text">-->
<!--                            Ipsum, nec sagittis sem nibh id elit is sed-->
<!--                            odio sit amet nibhp psum, nec sagittis sem-->
<!--                        </div>-->
<!--                    </li>-->
<!--                </ul>-->
<!--            </div>-->
<!---->
<!---->







<!--<!-- -->
<!--                --><?php
//                the_content();
//                ?><!-- -->



<!-- END PAGE CONTENT -->

            </div>
        <?php endwhile; ?>
    </div>


<?php get_footer(); ?>