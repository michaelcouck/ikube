<?php
class Artvens_Theme_Helper
{
    public static $instance = null;
    public $page_path = "";
    public $is_home = false;


    function Artvens_Theme_Helper()
    {
        add_action( 'after_setup_theme', array( &$this, 'do_theme_setup' ) );
        add_filter( 'wp_title', array( &$this, 'filter_wp_title'), 10, 2 );
        add_filter( 'excerpt_more', array( &$this, 'auto_excerpt_more') );
        add_filter( 'get_the_excerpt', array( &$this, 'custom_excerpt_more') );
        add_action('init', array( &$this, 'theme_init') );
        add_action( 'widgets_init', array( &$this, 'widgets_init') );
        add_filter('body_class', array( &$this, 'add_slug_to_body_class')); // Add slug to body class (Starkers build)
		remove_action( 'wp_head' , 'wp_generator' );
        remove_filter('the_excerpt', 'wpautop'); // Remove <p> tags from Excerpt altogether
        self::$instance = &$this;
    }

    public function init_frontend(){
        $this->page_path = $this->get_page_path();
        $this->is_home = is_front_page();
    }

    public function theme_init() {
        wp_enqueue_script('jquery');
    }


    public function add_slug_to_body_class($classes) {
        global $post;
        if (is_home()) {
            $key = array_search('home', $classes);
            if ($key > -1) {
                unset($classes[$key]);
            }
        } elseif (is_page()) {
            $classes[] = sanitize_html_class($post->post_name);
        } elseif (is_singular()) {
            $classes[] = sanitize_html_class($post->post_name);
        }

        return $classes;
    }

    /**
     * Sets up theme defaults and registers support for various WordPress features.
    */
    public function do_theme_setup() {
        add_editor_style();

        add_theme_support( 'post-thumbnails' );
        add_theme_support( 'automatic-feed-links' );

        load_theme_textdomain( 'artvens_theme', TEMPLATEPATH . '/languages' );

        ///TODO menus registration based on config files
        register_nav_menu('primary', __( 'Primary Navigation', 'artvens_theme' ));
        register_nav_menu('secondary', __( 'Secondary Navigation', 'artvens_theme' ));

        include_once(ABSPATH . 'wp-admin/includes/plugin.php');
    }

    //Display active manu class
    //<li display_active_menu_class('')><a href="/">Home</a></li>
    //<li display_active_menu_class('page-name')><a href="/page-name/">Page-Name</a></li>
    public function display_active_menu_class($menu_link, $class_name_only = false, $active_class = "active")
    {
        if($menu_link == "")
        {
            if($this->is_home == true)
                $this->display_active_mlink($class_name_only, $active_class);
        }
        else if(is_page($menu_link))
        {
            $this->display_active_mlink($class_name_only, $active_class);
        }
        else if(is_home() && $menu_link == 'blog')
   		{
   			$this->display_active_mlink($class_name_only, $active_class);
   		}        
    }


    //Return url for page featured image, if image not set return false
    public function get_post_featured_image($post = null)
    {
        $post = $this->get_post_object($post);
        $image = wp_get_attachment_image_src( get_post_thumbnail_id( $post->ID ), 'single-post-thumbnail' );
        if($image[0])
        {
            return $image[0];
        }
        else
        {
            return false;
        }
    }

    //Display pagination block
    public function display_pagination($pages = '', $range = 2)
    {
        $showitems = ($range * 2)+1;

        global $paged;
        if(empty($paged)) $paged = 1;

        if($pages == '')
        {
            global $wp_query;
            $pages = $wp_query->max_num_pages;
            if(!$pages)
            {
                $pages = 1;
            }
        }

        if(1 != $pages)
        {
            Artvens_Template_Manager::pagination_prev($paged, $pages, $range, $showitems);

            for ($i=1; $i <= $pages; $i++)
            {
                if (1 != $pages &&( !($i >= $paged+$range+1 || $i <= $paged-$range-1) || $pages <= $showitems ))
                {
                    Artvens_Template_Manager::pagination_item($paged, $i);
                }
            }

            Artvens_Template_Manager::pagination_next($paged, $pages, $range, $showitems);
        }
    }

    //Displays recent posts;
    public function display_recent_posts($count = 4)
    {
        Artvens_Template_Manager::recent_posts_start();
        query_posts("orderby=date&order=DESC&posts_per_page=$count");
        if(have_posts())
        {
            while (have_posts()) : the_post();
                Artvens_Template_Manager::recent_posts_list_item();
            endwhile;
        }
        wp_reset_query();
        Artvens_Template_Manager::recent_posts_end();
    }

    //Get shortened excerpt
    public function the_excerpt_max_charlength($charlength, $continue_string = "...") {
        $excerpt = get_the_excerpt();
        $charlength++;

        if ( mb_strlen( $excerpt ) > $charlength ) {
            $subex = mb_substr( $excerpt, 0, $charlength - 5 );
            $exwords = explode( ' ', $subex );
            $excut = - ( mb_strlen( $exwords[ count( $exwords ) - 1 ] ) );
            if ( $excut < 0 ) {
                echo mb_substr( $subex, 0, $excut );
            } else {
                echo $subex;
            }
            echo $continue_string;
        } else {
            echo $excerpt;
        }
    }


    public function display_related_posts($post = null)
    {
        $post = &get_post_object($post);
        $category_name = get_post_meta_data($post, 'category_name', '');
        $this->display_posts_by_category_name($category_name);
    }

    public function display_posts_by_category_name($category_name)
    {
        if(trim($category_name) != "")
        {
            query_posts("category_name=$category_name&orderby=date&order=DESC&posts_per_page=5");
            if(have_posts())
            {
                while (have_posts()) : the_post();
                    ?>
                <div class="cell"><a class="footer-post-link" title="<?php the_title(); ?>" href="<?php the_permalink(); ?>"><?php the_title(); ?></a></div>
                <?php
                endwhile;
            }
            wp_reset_query();
        }
    }

    //Display tags cloud
    public function display_tags_cloud($count = 10)
    {
        $args["number"] = $count;
        $tags = get_tags($args);
        $html = "";
        foreach ($tags as $tag){
            $tag_link = get_tag_link($tag->term_id);

            $html .= "<a href='{$tag_link}' title='{$tag->name} Tag' class='{$tag->slug}'>";
            $html .= "{$tag->name}</a>";
        }
        echo $html;
    }

    /*Return post metadata object*/
    public function get_post_meta_data($post, $name, $default)
    {
        $ret_result=get_post_meta($post->ID, $name, true);
        if(!$ret_result)
        {
            $ret_result=get_option($name);
            if(!$ret_result)
            {
                $ret_result=$default;
            }
        }
        return $ret_result;
    }


    public function display_menu($location_id, $container_id = "")
    {
        wp_nav_menu( array('theme_location' => $location_id, 'menu_id' => $container_id, 'container' => '', 'fallback_cb' => 'none' ));
    }

    //Display sub-pages
    public function display_sub_pages($post_id = false)
    {
        global $post;
        if($post){
            if($post->post_parent){
                $post_id = $post->post_parent;
            }
            else{
                $post_id = $post->ID;
            }
        }

        if($post_id){
            $children = wp_list_pages("title_li=&child_of=".$post_id."&echo=0");
            if ($children)
            {
                return "<ul>$children</ul>";
            }
        }
        return false;
    }

	//Shorten input string for $allowed_words_count
	public function shorten_text($text, $allowed_words_count)
    {
        $text = strip_shortcodes( $text );
        $text = strip_tags($text);
        $words = explode(' ', $text, $allowed_words_count + 1);
        if (count($words) > $allowed_words_count) {
            array_pop($words);
            $text = implode(' ', $words);
        }
        return $text;
    }

    /*Makes some changes to the <title> tag, by filtering the output of wp_title().*/
    public function filter_wp_title( $title, $separator ) {
        $separator = "|";
        // Don't affect wp_title() calls in feeds.
        if ( is_feed() )
            return $title;

        // The $paged global variable contains the page number of a listing of posts.
        // The $page global variable contains the page number of a single post that is paged.
        // We'll display whichever one applies, if we're not looking at the first page.
        global $paged, $page;

        if ( is_search() ) {
            // If we're a search, let's start over:
            $title = sprintf( __( 'Search results for %s', 'artvens_theme' ), '"' . get_search_query() . '"' );
            // Add a page number if we're on page 2 or more:
            if ( $paged >= 2 )
                $title .= " $separator " . sprintf( __( 'Page %s', 'artvens_theme' ), $paged );
            // Add the site name to the end:
            $title .= " $separator " . get_bloginfo( 'name', 'display' );
            // We're done. Let's send the new title back to wp_title():
            return $title;
        }

        // Otherwise, let's start by adding the site name to the end:
        if (is_plugin_active('all-in-one-seo-pack/all_in_one_seo_pack.php')) {
            //if plugin installed add name only on home pages.
            if ( is_home() || is_front_page())
                $title .= get_bloginfo( 'name', 'display' );
        }
        else{
            $title .= get_bloginfo( 'name', 'display' );
        }

        // If we have a site description and we're on the home/front page, add the description:
        $site_description = get_bloginfo( 'description', 'display' );
        if ( $site_description && ( is_home() || is_front_page() ) )
            $title .= " $separator " . $site_description;

        // Add a page number if necessary:
        if ( $paged >= 2 || $page >= 2 )
            $title .= " $separator " . sprintf( __( 'Page %s', 'artvens_theme' ), max( $paged, $page ) );

        // Return the new title to wp_title():
        return $title;
    }

    /**
     * Returns a "Continue Reading" link for excerpts
     *
     * @since artvens_theme 1.0
     * @return string "Continue Reading" link
     */
    public function continue_reading_link() {
        return ' <a href="'. get_permalink() . '">' . __( 'Continue reading <span class="meta-nav">&rarr;</span>', 'artvens_theme' ) . '</a>';
    }

    /**
     * Replaces "[...]" (appended to automatically generated excerpts) with an ellipsis and artvens_theme_continue_reading_link().
     *
     * To override this in a child theme, remove the filter and add your own
     * function tied to the excerpt_more filter hook.
     *
     * @since artvens_theme 1.0
     * @return string An ellipsis
     */
    public function auto_excerpt_more( $more ) {
        return ' &hellip;' . $this->continue_reading_link();
    }

    /**
     * Adds a pretty "Continue Reading" link to custom post excerpts.
     *
     * To override this link in a child theme, remove the filter and add your own
     * function tied to the get_the_excerpt filter hook.
     *
     * @since artvens_theme 1.0
     * @return string Excerpt with a pretty "Continue Reading" link
     */
    public function custom_excerpt_more( $output ) {
        if ( has_excerpt() && ! is_attachment() ) {
            $output .= $this->continue_reading_link();
        }
        return $output;
    }

    /**
     * Register widgetized areas, including two sidebars and four widget-ready columns in the footer.
     *
     * To override artvens_theme_widgets_init() in a child theme, remove the action hook and add your own
     * function tied to the init hook.
     *
     * @since artvens_theme 1.0
     * @uses register_sidebar
     */
    public function widgets_init() {
        // Area 1, located at the top of the sidebar.
        register_sidebar( array(
            'name' => __( 'Primary Widget Area', 'artvens_theme' ),
            'id' => 'primary-widget-area',
            'description' => __( 'The primary widget area', 'artvens_theme' ),
            'before_widget' => '<li id="%1$s" class="widget-container %2$s">',
            'after_widget' => '</li>',
            'before_title' => '<div class="sidebar-caption"><div class="left-bg"></div><div class="text">',
            'after_title' => '</div><div class="right-bg"></div><div class="clear"></div></div>',
        ) );
    }

    /*Return current page path*/
    private function get_page_path()
    {
		$var = explode('?', $_SERVER['REQUEST_URI']);
        $exploded = trim(array_shift($var), '/');
        list($current_page_link,) = explode('/', $exploded);
        $current_page_link = (!empty($current_page_link) ? $current_page_link : 'home');
        return $current_page_link;
    }

    //Display active menu link
    private function display_active_mlink($class_name_only, $active_class)
    {
        if($class_name_only)
            echo $active_class;
        else
            echo "class='$active_class'";
    }

    //Get post object
    private function get_post_object($post_param = null)
    {
        if($post_param == null)
        {
            global $post;
            $post_obj = &$post;
        }
        else
        {
            $post_obj = &$post_param;
        }
        return $post_obj;
    }
}