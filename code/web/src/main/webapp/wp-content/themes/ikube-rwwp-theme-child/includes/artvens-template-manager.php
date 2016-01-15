<?php
class Artvens_Template_Manager
{
    public static $ath = null;

    public static function recent_posts_start(){
        ?><ul><?php
    }

    public static function recent_posts_end(){
        ?></ul><?php
    }

    public static function recent_posts_list_item(){
        ?>
        <li>
            <div class="caption"><a class="footer-post-link" title="<?php the_title(); ?>" href="<?php the_permalink(); ?>"><?php the_title(); ?></a></div>
            <div class="text"><?php self::$ath->the_excerpt_max_charlength(120); ?></div>
        </li>
        <?php
    }

    public static function reviews_start(){
        ?><ul class="reviews"><?php
    }

    public static function reviews_end(){
        ?></ul><?php
    }

    public static function reviews_list_item($review_item){
        ?>
        <li class="review-item">
            <div class="separator"></div>
            <div class="item">
                <div class="caption">
                    <a href="<?php echo $review_item->read_more_link; ?>"><?php echo $review_item->title; ?></a>
                </div>
                <div class="second-line">
                    <div class="stars"><?php $review_item->helper->build_review_stars($review_item->rating_value); ?></div>
                    <div class="date"><?php echo date("M d, Y", strtotime($review_item->date)); ?></div>
                    <div class="author">by&nbsp;<?php echo $review_item->name; ?></div>
                    <div class="clear"></div>
                </div>
                <div class="text">
                    <?php echo $review_item->text; ?>
                </div>
            </div>
        </li>
    <?php
    }

    public static function pagination_prev($paged, $pages, $range, $show_items){
        echo "<div class='pagination'>";
        if($paged > 2 && $paged > $range+1 && $show_items < $pages) echo "<a href='".get_pagenum_link(1)."'>&laquo;</a>";
        if($paged > 1 && $show_items < $pages) echo "<a href='".get_pagenum_link($paged - 1)."'>&lsaquo;</a>";
    }

    public static function pagination_next($paged, $pages, $range, $show_items){
        if ($paged < $pages && $show_items < $pages) echo "<a href='".get_pagenum_link($paged + 1)."'>&rsaquo;</a>";
        if ($paged < $pages-1 &&  $paged+$range-1 < $pages && $show_items < $pages) echo "<a href='".get_pagenum_link($pages)."'>&raquo;</a>";
        echo "</div>\n<div class='clear'></div>\n";
    }

    public static function pagination_item($paged, $i){
        echo ($paged == $i)? "<span class='current'>".$i."</span>":"<a href='".get_pagenum_link($i)."' class='inactive' >".$i."</a>";
    }

    public static function display_comments( $comment, $args, $depth ) {
        $GLOBALS['comment'] = $comment;
        switch ( $comment->comment_type ) :
            case 'pingback' :
            case 'trackback' :
                // Display trackbacks differently than normal comments.
                ?>
                <li <?php comment_class(); ?> id="comment-<?php comment_ID(); ?>">
                <p><?php _e( 'Pingback:' ); ?> <?php comment_author_link(); ?> <?php edit_comment_link( __( '(Edit)' ), '<span class="edit-link">', '</span>' ); ?></p>
                <?php
                break;
            default :
                // Proceed with normal comments.
                global $post;
                ?>
                <li <?php comment_class(); ?> id="li-comment-<?php comment_ID(); ?>">
                    <article id="comment-<?php comment_ID(); ?>" class="comment">
                        <header class="comment-meta comment-author vcard">
                            <?php
                            echo get_avatar( $comment, 44 );
                            printf( '<cite class="fn">%1$s %2$s</cite>',
                                get_comment_author_link(),
                                // If current post author is also comment author, make it known visually.
                                ( $comment->user_id === $post->post_author ) ? '<span> ' . __( 'Post author' ) . '</span>' : ''
                            );
                            printf( '<a href="%1$s"><time datetime="%2$s">%3$s</time></a>',
                                esc_url( get_comment_link( $comment->comment_ID ) ),
                                get_comment_time( 'c' ),
                                /* translators: 1: date, 2: time */
                                sprintf( __( '%1$s at %2$s' ), get_comment_date(), get_comment_time() )
                            );
                            ?>
                        </header><!-- .comment-meta -->

                        <?php if ( '0' == $comment->comment_approved ) : ?>
                            <p class="comment-awaiting-moderation"><?php _e( 'Your comment is awaiting moderation.' ); ?></p>
                        <?php endif; ?>

                        <section class="comment-content comment">
                            <?php comment_text(); ?>
                            <?php edit_comment_link( __( 'Edit' ), '<p class="edit-link">', '</p>' ); ?>
                        </section><!-- .comment-content -->

                        <div class="reply">
                            <?php comment_reply_link( array_merge( $args, array( 'reply_text' => __( 'Reply' ), 'after' => ' <span>&darr;</span>', 'depth' => $depth, 'max_depth' => $args['max_depth'] ) ) ); ?>
                        </div><!-- .reply -->
                        <div class="clear"></div>
                    </article><!-- #comment-## -->
                <?php
                break;
        endswitch; // end comment_type check
    }

}