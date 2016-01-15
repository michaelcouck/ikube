<?php
/**
 * The template for displaying 404 pages (Not Found).
 *
 * @package WordPress
 * @subpackage Twenty_Ten
 * @since Twenty Ten 1.0
 */

get_header();
?>
<div id="text-content">

    <div id="content-title">
        <div class="caption">
        	<h1>404</h1>
        </div>
        <div class="sub-caption">Whoops! This page does not exist.</div>
    </div>

    <div class="not-found-page column">
        <div class="caption">
            Seems like you ran off course. Let us guide you …
            try links below to help you find your way.
        </div>
        <div class="image">
            <div class="pillar"></div>
            <div id="pointer-left">
                <a href="/">Go to home page</a>
            </div>
            <div class="pillar"></div>
            <div id="pointer-center">
                <a href="/contact/">Request a consultation</a>
            </div>
            <div class="pillar"></div>
            <div id="pointer-right">
                <a href="/services/">Our services</a>
            </div>
            <div class="pillar big"></div>
        </div>

    </div>

</div>
<?php get_footer(); ?>