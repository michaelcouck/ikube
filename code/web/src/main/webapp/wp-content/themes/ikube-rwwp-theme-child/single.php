<?php
/**
 * The Template for displaying all single posts.
 */

get_header();
?>

    <div id="text-content">
        <?php if ( have_posts() ) while ( have_posts() ) : the_post(); ?>
            <div id="content-title">
                <div class="caption"><h1><?php the_title(); ?></h1></div>
                <div class="sub-caption">This is sub-caption</div>
            </div>
            <div class="content column">
                <?php
                the_content();
                ?>
            </div>
            <div id="nav-below" class="navigation">
                <div class="nav-previous"><?php previous_post_link( '%link', '<span class="meta-nav">' . _x( '&larr;', 'Previous post link', 'theros' ) . '</span> %title' ); ?></div>
                <div class="nav-next"><?php next_post_link( '%link', '%title <span class="meta-nav">' . _x( '&rarr;', 'Next post link', 'theros' ) . '</span>' ); ?></div>
                <div class="clear"></div>
            </div>
            <!--
                <div id="comments-block">
                    <?php /*comments_template( '', true ); */?>
                </div>
            -->
        <?php endwhile; ?>
    </div>

<?php
    get_footer();
?>