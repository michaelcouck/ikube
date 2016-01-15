<?php
class Artvens_Theme_Admin
{
    private $customer_specific_group_name;
    function Artvens_Theme_Admin()
    {
        add_action('admin_init', array(&$this, 'theme_options_init' ));
        add_action("admin_menu", array(&$this, "theme_options_add_page"));
    }

    public function theme_options_init(){
        $this->customer_specific_group_name = "artvens-theme-settings-group-customer-specific";
        register_setting( $this->customer_specific_group_name, 'atv_phone_number' );
    }

    public function theme_options_add_page() {
        add_theme_page(__( 'Theme settings Artvens-Kronos', 'artvens-theme' ), __( 'Theme settings', 'artvens-theme' ), 'edit_theme_options', 'artvens-theme-settings', array(&$this, 'artvens_theme_settings') );
    }



    public function artvens_theme_settings() {
        $this->check_permission();
        $this->set_settings_updated();

        ?>
    <div class="wrap">
        <?php screen_icon('themes'); ?> <h2>Artvens Theme Settings</h2>
        <?php if ( $this->is_settings_updated() ) : ?>
        <div id="message" class="updated">
            <p><strong><?php _e('Settings saved.') ?></strong></p>
        </div>
        <?php endif; ?>

        <?php
        $active_tab = $this->get_active_tab();
        ?>

        <h2 class="nav-tab-wrapper">
            <?php
                $this->display_tab_link('customer-specific-settings', 'Customer Specific Settings');
                $this->display_tab_link('general-settings', 'General Settings');
            ?>
        </h2>
        <form method="post" action="options.php">
        <?php
            $tab_found = false;
            switch($active_tab){
                case 'general-settings':
                    //$this->general_settings_tab();
                    //$tab_found = true;
                    break;
                case 'customer-specific-settings':
                    $this->customer_specific_tab();
                    $tab_found = true;
                    break;
            }

            if($tab_found){
                submit_button();
            }
        ?>
        </form>
    </div>
<?php

    }

    private function customer_specific_tab(){
        settings_fields( $this->customer_specific_group_name );
        ?>
        <table class="form-table">
            <!-- <tr valign="top">
                <th scope="row" colspan="2">
                    <h3>Header contacts</h3>
                </th>
            </tr>
            <tr valign="top">
                <th scope="row">
                    <label for="phone_number">
                        Phone number:
                    </label>
                </th>
                <td>
                    <input type="text" name="atv_phone_number" size="50" value="<?php esc_attr_e( get_option('atv_phone_number') ); ?>" />
                </td>
            </tr>
           <tr valign="top">
                <th scope="row">
                    <label for="phone_number">
                        ????? ????????:
                    </label>
                </th>
                <td>
                    <input type="checkbox" name="atv_special_offer_exists" <?php /*checked( 'on', get_option('atv_special_offer_exists') );*/?> />
                </td>
            </tr>-->
        </table>
        <?php
    }

    private function check_permission(){
        if (!current_user_can('manage_options')) {
            wp_die('You do not have sufficient permissions to access this page.');
        }
    }

    private function set_settings_updated(){
        if ( ! isset( $_REQUEST['settings-updated'] ) ) $_REQUEST['settings-updated'] = false;
    }

    private function is_settings_updated(){
        return (false !== $_REQUEST['settings-updated']);
    }

    private function get_active_tab( $default_tab = 'customer-specific-settings' ){
        $active_tab = isset( $_GET[ 'tab' ] ) ? $_GET[ 'tab' ] : $default_tab;
        return $active_tab;
    }

    private function display_tab_link($tab_page, $tab_page_caption){
        $active_tab = $this->get_active_tab();
        ?>
        <a href="?page=artvens-theme-settings&tab=<?= $tab_page; ?>" class="nav-tab <?php echo $active_tab == $tab_page ? 'nav-tab-active' : ''; ?>"><?= $tab_page_caption; ?></a>
        <?php
    }
}

global $artvens_theme_admin;
$artvens_theme_admin = new Artvens_Theme_Admin();



