<?php

/*

Template Name: Contact

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

			
                <?php
                the_content();
                ?> 

<!-- END PAGE CONTENT -->            	

            </div>




        <?php endwhile; ?>
    </div>


<?php get_footer(); ?>