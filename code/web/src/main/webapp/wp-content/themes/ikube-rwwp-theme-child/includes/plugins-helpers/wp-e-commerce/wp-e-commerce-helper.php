<?php
class Wp_E_Commerce_Helper
{
    const product_page = "catalog/";
    private $need_close_list = false;
    private $last_root_category;
    private $options;

    public function Wp_E_Commerce_Helper()
    {
        global $theme_options;
        $this->options = &$theme_options;
    }

    public function show_specials_products_from_category($category_id, $count = 2)
    {
        $product_thumb_size = array("width"=>78, "height"=>98);
        $products = $this->get_products_from_category($category_id, $count, $product_thumb_size);
        echo "<ul>";
        foreach($products as $product)
        {
            ?>
        <li>
            <a href="<?php echo $product->permalink; ?>">
                <div class="image">
                    <img src="<?php echo $product->thumbnail; ?>" alt="<?php echo $product->title; ?>"/>
                </div>
                <div class="text">
                    <div class="caption"><?php echo $this->shorter_text($product->title, 20); ?></div>
                    <div class="description"><?php echo $this->shorter_text($product->description, 52); ?></div>
                    <div class="price">$<?php echo $product->get_actual_price(); ?></div>
                </div>
                <div class="clear"></div>
            </a>
        </li>
        <?php
        }
        echo "</ul>";
    }

    public function show_featured_products_from_category($category_id, $count = 3)
    {
        $product_thumb_size = array("width"=>175, "height"=>175);
        $products = $this->get_products_from_category($category_id, $count, $product_thumb_size);
        foreach($products as $product)
        {
            ?>

        <div class="column first-column">
            <div class="inner">
                <h3><a href="<?php echo $product->permalink; ?>">New Paul Mitchell Pro Tools</a></h3>
                <a href="<?php echo $product->permalink; ?>"><img src="<?php echo $product->thumbnail; ?>" alt="<?php echo $product->title; ?>" width="175" height="175" /></a>
                <p><?php echo $this->shorter_text($product->description, 100); ?></p>
            </div>
        </div>
        <?php
        }
    }

    public function get_products_from_category($category_id, $count, $product_thumb_size)
    {
        $query = $this->create_products_query($category_id, $count, "rand", $product_thumb_size);
        $products = $this->get_products_array($query);
        return $products;
    }

    public function show_categories_callback($category, $level, $fieldconfig)
    {
        if($this->category_excluded($category))
            return;

        if($level == 0)
        {
            if(strlen($category->name) > 17)
                $menu_class = "nav-caption-big";
            else
                $menu_class = "nav-caption";

            if($this->need_close_list)
            {
                ?>
            </ul>
            </li>
            <?php
            }
            ?>
                            <li>
                                <div class="<?php echo $menu_class; ?>"><a href="/<?php echo self::product_page.$category->slug; ?>/"><?php echo $category->name; ?></a></div>
                                <ul>
            <?php
            $this->need_close_list = true;
        }
        if($level > 0)
        {
            ?>
            <li>
                <a href="/<?php echo self::product_page.$category->slug; ?>/"><?php echo $category->name; ?></a>
            </li>
        <?php
        }
    }

    public function wpsc_shopping_basket_internals( $cart, $quantity_limit = false, $no_title=false )
    {
        global $wpdb;

        $display_state = '';
        if ( ( ( ( isset( $_SESSION['slider_state'] ) && $_SESSION['slider_state'] == 0) ) || ( wpsc_cart_item_count() < 1 ) ) && ( get_option( 'show_sliding_cart' ) == 1 ) )
            $display_state = "style='display: none;'";
        echo "    <div id='sliding_cart' class='shopping-cart-wrapper' $display_state>";
        include_once( wpsc_get_template_file_path( 'wpsc-cart_widget.php' ) );
        echo "    </div>";
    }

    private function category_excluded($category)
    {
        $excluded_ids = array(5, 10, 41, 43, 45, 46);
        if(in_array($category->term_id, $excluded_ids) || in_array($category->parent, $excluded_ids))
            return true;
        else
            return false;
    }

    private function create_products_query($category_ids, $products_count, $order_by = "rand", $product_thumb_size)
    {
        if(!is_array($category_ids))
        {
            $id = $category_ids;
            $product_categories = array();
            $product_categories[] = $id;
        }
        else
        {
            $product_categories = $category_ids;
        }

        $product_query = array(
            'post_type' => 'wpsc-product',
            'post_status' => 'publish',
            'posts_per_page' => $products_count,
            'orderby' => $order_by,
            'tax_query' => array(
                array(
                    'taxonomy' => 'wpsc_product_category',
                    'field' => 'id',
                    'terms' => $product_categories
                )
            ),
            'thumb_size' => $product_thumb_size
        );
        return $product_query;
    }

    private function get_products_array($query)
    {
        $image_width = $query['thumb_size']['width'];
        $image_height = $query['thumb_size']['height'];
        $products_query_results = new WP_Query( $query );
        $products_array = array();
        if($products_query_results->have_posts())
        {
            while ( $products_query_results->have_posts() )
            {
                $products_query_results->the_post();
                $product_object = new Wp_E_Product();

                $product_object->id = get_the_ID();

                $product_object->title = wpsc_the_product_title();
                $product_object->description = wpsc_the_product_description();
                $product_object->additional_description = wpsc_the_product_additional_description();
                $product_object->thumbnail = wpsc_the_product_thumbnail($image_width, $image_height, $product_object->id);
                if(!$product_object->thumbnail)
                {
                    $product_object->thumbnail = WPSC_CORE_THEME_URL."wpsc-images/noimage.png";
                }
                $product_object->permalink = wpsc_the_product_permalink();
                $product_object->categories = $query['tax_query'][0]['terms'];
                $product_object->regular_price = wpsc_calculate_price( $product_object->id, false, false );
                $product_object->sale_price = wpsc_calculate_price( $product_object->id, false, true );
                $products_array[] = $product_object;
            }
        }
        // Reset Post Data
        wp_reset_postdata();
        return $products_array;
    }

    function shorter_text($text, $chars_limit)
    {
        // Check if length is larger than the character limit
        if (strlen($text) > $chars_limit)
        {
            // If so, cut the string at the character limit
            $new_text = substr($text, 0, $chars_limit);
            // Trim off white space
            $new_text = trim($new_text);
            // Add at end of text ...
            return $new_text . "...";
        }
        // If not just return the text as is
        else
        {
            return $text;
        }
    }
}