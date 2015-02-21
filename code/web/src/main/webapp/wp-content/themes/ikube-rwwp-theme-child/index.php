<?php
    get_header();
?>
    <div id="text-content" class="blog">
        <?php if ( have_posts() ) while ( have_posts() ) : the_post(); ?>
            <div id="content-title">
                <div class="caption"><h1><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h1></div>
                <div class="sub-caption">This is sub-caption</div>
            </div>
            <div class="content column">
                <?php
                the_content("Continue reading... ");
                ?>
            </div>
        <?php endwhile; ?>
        <?php get_template_part('pagination'); ?>
    </div>
<?php
    get_footer();
?>