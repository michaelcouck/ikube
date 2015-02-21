<?php
/**
 * The template for displaying Search Results pages.
 */

get_header();
?>

<div id="text-content">
    <?php if ( have_posts() ) : ?>
        <div class="spacer"></div>
        <div class="caption"><h1><?php printf( __( 'Search Results for: %s', 'theros' ), '<span>' . get_search_query() . '</span>' ); ?></h1></div>
        <div class="spacer"></div>
        <?php if ( have_posts() ) while ( have_posts() ) : the_post();?>
            <div class="caption"><h2><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h2></div>
            <div class="text">
            <?php the_content("Read more... "); ?>
            </div>
        <?php
            endwhile;
        ?>
    <?php else : ?>
        <div class="spacer"></div>
        <div class="caption"><h1 class="entry-title"><?php _e( 'Nothing Found', 'theros' ); ?></h1></div>
        <div class="spacer"></div>
        <div class="text">
        <p><?php _e( 'Sorry, but nothing matched your search criteria. Please try again with some different keywords.', 'twentyten' ); ?></p>
        </div>
    <?php endif; ?>
</div>

<?php
    get_footer();
?>



