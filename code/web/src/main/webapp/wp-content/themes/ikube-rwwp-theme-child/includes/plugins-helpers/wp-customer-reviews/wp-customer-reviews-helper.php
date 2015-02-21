<?php
class Wp_Customer_Reviews_Helper {

    function Wp_Customer_Reviews_Helper(){
        add_action('wp_enqueue_scripts', array(&$this,'register_styles') );
    }

    public function register_styles(){
        wp_register_style( 'wpcrh-style',
            get_template_directory_uri() . '/includes/plugins-helpers/wp-customer-reviews/helper_style.css',
            array(),
            '20120208',
            'all' );

        wp_enqueue_style( 'wpcrh-style' );
    }

    public function display_wp_reviews($post_id, $reviews_count)
    {
        global $WPCustomerReviews;
        if($WPCustomerReviews != null)
        {
            $ret_res = "";
            $arr_reviews = $WPCustomerReviews->get_reviews($post_id, 1, $reviews_count, 1);
            $reviews = $arr_reviews[0];
            Artvens_Template_Manager::reviews_start();
            foreach ($reviews as $review)
            {
                $review_item = new stdClass();
                $review_item->title = $review->review_title;
                $review_item->rating_value = $review->review_rating;
                $review_item->name = $review->reviewer_name;
                $review_item->date = $review->date_time;
                $review_item->text = $this->shorten_text($review->review_text, 12)."...";
                $review_item->read_more_link = $WPCustomerReviews->get_jumplink_for_review($review,1);
                $review_item->helper = &$this;
                Artvens_Template_Manager::reviews_list_item($review_item);
            }
            Artvens_Template_Manager::reviews_end();
        }
        else
        {
            echo "WP-CustomerReviews is not installed";
        }
    }

    public function build_review_stars($review_value)
    {
        for($i=1; $i<=5; $i++)
        {
            if($i <= $review_value):
                ?>
            <img src="<?php echo get_bloginfo( 'stylesheet_directory', 'display' )?>/images/reviews/review_star.png" alt=""/>
            <?php
            else:
                ?>
            <img src="<?php echo get_bloginfo( 'stylesheet_directory', 'display' )?>/images/reviews/review_star_blank.png" alt=""/>
            <?php
            endif;
        }
    }

    private function shorten_text($text, $allowed_words_count)
    {
        global $artvens_theme_info;
        return $artvens_theme_info->shorten_text($text, $allowed_words_count);
    }
}

global $wpcrh;
$wpcrh = new Wp_Customer_Reviews_Helper();