<?php
/*
 * All functions should have atv prefix.
*/

function atv_init(){
    Artvens_Theme_Helper::$instance->init_frontend();
}

function atv_is_home_page(){
    return Artvens_Theme_Helper::$instance->is_home;
}

function atv_display_menu($location_id, $container_id = ""){
    Artvens_Theme_Helper::$instance->display_menu($location_id, $container_id);
}

function atv_display_recent_posts($count = 4){
    Artvens_Theme_Helper::$instance->display_recent_posts($count);
}

function atv_display_reviews($reviews_page_id, $reviews_count = 2){
    global $wpcrh;
    $wpcrh->display_wp_reviews($reviews_page_id, $reviews_count);
}

function atv_get_post_featured_image(){
    return Artvens_Theme_Helper::$instance->get_post_featured_image();
}

function atv_display_comments($comment, $args, $depth){
    Artvens_Template_Manager::display_comments($comment, $args, $depth);
}


function atv_pagination()
{
    global $wp_query;
    $big = 999999999;
    echo paginate_links(array(
        'base' => str_replace($big, '%#%', get_pagenum_link($big)),
        'format' => '?paged=%#%',
        'current' => max(1, get_query_var('paged')),
        'total' => $wp_query->max_num_pages
    ));
}

// Custom Comments Callback
function atv_comments($comment, $args, $depth)
{
    $GLOBALS['comment'] = $comment;
    extract($args, EXTR_SKIP);

    if ( 'div' == $args['style'] ) {
        $tag = 'div';
        $add_below = 'comment';
    } else {
        $tag = 'li';
        $add_below = 'div-comment';
    }
    ?>
    <!-- heads up: starting < for the html tag (li or div) in the next line: -->
    <<?php echo $tag ?> <?php comment_class(empty( $args['has_children'] ) ? '' : 'parent') ?> id="comment-<?php comment_ID() ?>">
    <?php if ( 'div' != $args['style'] ) : ?>
    <div id="div-comment-<?php comment_ID() ?>" class="comment-body">
<?php endif; ?>
    <div class="comment-author vcard">
        <?php if ($args['avatar_size'] != 0) echo get_avatar( $comment, $args['180'] ); ?>
        <?php printf(__('<cite class="fn">%s</cite> <span class="says">says:</span>'), get_comment_author_link()) ?>
    </div>
    <?php if ($comment->comment_approved == '0') : ?>
    <em class="comment-awaiting-moderation"><?php _e('Your comment is awaiting moderation.') ?></em>
    <br />
<?php endif; ?>

    <div class="comment-meta commentmetadata"><a href="<?php echo htmlspecialchars( get_comment_link( $comment->comment_ID ) ) ?>">
            <?php
            printf( __('%1$s at %2$s'), get_comment_date(),  get_comment_time()) ?></a><?php edit_comment_link(__('(Edit)'),'  ','' );
        ?>
    </div>

    <?php comment_text() ?>

    <div class="reply">
        <?php comment_reply_link(array_merge( $args, array('add_below' => $add_below, 'depth' => $depth, 'max_depth' => $args['max_depth']))) ?>
    </div>
    <?php if ( 'div' != $args['style'] ) : ?>
    </div>
<?php endif; ?>
<?php }