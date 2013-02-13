<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Quick start</h2>
	
	To get Ikube running in less than 5 minutes.<br>
	
	1) Down-load the latest version at <a href="http://code.google.com/p/ikube/downloads/list" target="_top">ikube</a>.<br>
	2) Down-load the acompanying <a href="http://code.google.com/p/ikube/downloads/list" target="_top">Tomcat</a>.<br>
	3) Copy the ikube.war file to the $TOMCAT_INSTALL/webapps directory of Tomcat.<br>
	4) The default configuration is already in the bin directory, $TOMCAT_INSTALL/bin/ikube<br>
	5) Change the name of the war to ikube.war if not already so.<br>
	6) Start Tomcat (with the ./startup.sh or startup.bat files). <br>
	7) Have a cup of coffee.<br><br>
			
	That's all. Indexing will start, first the local documentation, then the default indexes like the Geospatial index(which is on the 
	development server). Please make sure that Tomcat has at least 1.5 gig of memory, for large indexes this can go up to 8 gig depending 
	on configuration.<br><br>
			
	You can go to <a href="http://localhost:8080/ikube">user interface</a> to monitor how the indexes are coming along.<br><br> 
			 
	The default wait period for the scheduler to start is generally around five minutes. Of course you can change this we'll get into this in 
	the configuration section.<br><br>
			 
</div>