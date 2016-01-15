<?php
/**
 * This file include all necessaries files for Artvens theme.
 * And creates required objects
 */

include_once ARTVENS_THEME_PATH.'/artvens-theme-config.php';
include_once ARTVENS_THEME_PATH.'/theme-core/artvens-theme-helper.php';
include_once ARTVENS_THEME_PATH.'/artvens-template-manager.php';
include_once ARTVENS_THEME_PATH.'/artvens-theme-functions.php';

//Load theme helper
global $artvens_theme_info;
$artvens_theme_info = new Artvens_Theme_Helper();
Artvens_Template_Manager::$ath = &$artvens_theme_info;

//Load plugin helpers
global $artvens_plugin_helpers;

if(is_array($artvens_plugin_helpers)){
    foreach($artvens_plugin_helpers as $key => $value){
        if($value == true){
            $helper_path  = ARTVENS_THEME_PATH."/plugins-helpers/".$key."/".$key."-helper.php";
            include_once $helper_path;
        }
    }
}


//Load theme settings in dashboard
global $artvens_theme_dashboard_support;

if($artvens_theme_dashboard_support){
    include_once ARTVENS_THEME_PATH.'/theme-core/artvens-theme-admin.php';
}