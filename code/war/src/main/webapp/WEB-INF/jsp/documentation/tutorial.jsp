<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">tutorial</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			<strong>tutorial</strong>&nbsp;
			 This is the step by step tutorial for setting up Ikube with an external configuration.<br><br>
			 
			 1) Download the <a href="http://code.google.com/p/ikube/downloads/list"">latest</a> version<br>
			 2) Download the <a href="http://code.google.com/p/ikube/downloads/list"">configuration.jar</a><br>
			 3) Download <a href="http://tomcat.apache.org/download-70.cgi"">Tomcat</a><br>
			 4) Rename the ikube-2.0.0-war to ikube.war for the url to be simpler<br>
			 5) Copy the ikube.war to the webapps folder in the Tomcat install folder<br>
			 6) Unpack the configuration.jar into the bin directory of the Tomcat installation<br>
			 7) Go to the spring.xml file in the bin directory of the Tomcat installation, open it and delete the imports of 
			 	spring-ikube.xml and spring-geospatial.xml<br>
			 8) Go to the bin directory of the Tomcat installation then the client folder open the spring-client.xml<br>
			 9) Delete the imports of the data source configuration files<br>
			 10) In the 'indexables' list delete all the entries except for the 'internet' entry<br>
			 11) Delete all the beans except for the 'internet' bean<br>
			 12) In the 'internet' bean change the property to your web site's url, something like 
			 	http://yourip.com/pages. Not that this will crawl the url that you specify so make sure that 
			 	there are not too many pages on the site, like IBM for example or you will have to wait a very 
			 	long time for the results<br>
			 13) Start the Tomcat<br>
			 14) Go to the <a href="http://localhost:8080/ikube/admin/servers.html">monitoring</a> page<br>
			 15) Wait until the 'working' flag is false by refreshing the page periodically<br>
			 16) Click on the 'default' link to go to the search page<br>
			 17) In the 'content' field type your search string, could be an arbitrary Lucene syntax string 
			 	like 'Michael~ OR Couck~ AND enterprise AND search'<br>
			 18) View the results at the bottom of the page<br>
			 19) For help and information please note that there is a Google Group for Ikube :)<br>
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			
		</td>
	</tr>
</table>
