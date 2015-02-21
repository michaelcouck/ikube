<?php

/*

Template Name: Resources

*/

?>



<?php get_header(); ?>

    <div id="text-content">
        <?php if ( have_posts() ) while ( have_posts() ) : the_post(); ?>
            <div id="content-title">
                <div class="caption"><h1><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h1></div>
                <div class="sub-caption">This is sub-caption</div>
            </div>
            <div class="content column">
<!-- PAGE CONTENT --> 

				<!-- <div id="resources">
					<div class="row first">
						<ul>
							<li>
								<div class="caption">Lorem ipsum dolor</div>
								<div class="text">Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 15. Proin gravida nibh vel velit auctor aliquet.</div>
								<div class="button">
									<a href="#">Download iKube for free</a>
								</div>
							</li>
							<li>
								<div class="image">
									<img src="<?php echo get_stylesheet_directory_uri(); ?>/images/safari-browser.png" alt="">
								</div>
							</li>
						</ul>
					</div>

					<div class="row second">
						<div class="caption">Check what can do ikube before download</div>
						<div class="text">Execute a query and get the analytics results online. It is key feature. Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 15. Lorem Ipsum is simply dummy text of the printing and typesetting industry.</div>
					</div>

					<div class="row third">
						<div class="caption">Podcasts</div>
						<div class="text">Some video of install instructions, configuring the analyzers, examples of executing sueries in Json and getting results, so some video stuff. I have no video at the moment, still have to do thaty.</div>
						<ul>
							<li>
								<img src="<?php echo get_stylesheet_directory_uri(); ?>/images/podcast1.png" alt="">
							</li>
							<li>
								<img src="<?php echo get_stylesheet_directory_uri(); ?>/images/podcast2.png" alt="">
							</li>
							<li>
								<img src="<?php echo get_stylesheet_directory_uri(); ?>/images/podcast3.png" alt="">
							</li>
						</ul>
						<div class="button">
							<a href="#">Check all podcasts</a>
						</div>
					</div>
				</div> -->

<!-- END PAGE CONTENT -->            	

            </div>
        <?php endwhile; ?>
    </div>


<?php get_footer(); ?>