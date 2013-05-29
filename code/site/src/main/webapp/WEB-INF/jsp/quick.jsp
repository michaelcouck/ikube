<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Quick start</h2>
	
	To get Ikube running in less than 5 minutes.<br>
	
	1) Down-load the latest version at <a href="http://ikube.be/artifactory/libs-release-local/ikube/ikube-war/4.2.2/ikube-war-4.2.2.war">ikube</a>.<br>
	2) Down-load the acompanying <a href="http://ikube.be/artifactory/libs-release-local/ikube/ikube-libs/4.2.2/ikube-libs-4.2.2.jar">configuration</a>.<br>
	3) Download <a href="http://tomcat.apache.org/">Tomcat</a> from the Apache site<br>
	4) Change the name of the war from ikube-war-xxx.war to ikube.war and copy the file to the $TOMCAT_INSTALL/webapps directory of Tomcat.<br>
	5) Unpack the configuration file (ikube-libs-4.2.0.jar) into the $TOMCAT_INSTALL/bin directory<br>
	6) Start Tomcat (with the ./startup.sh or startup.bat files). <br>
	7) Have a cup of coffee.<br><br>
			
	That's all. Indexing will start, first the local documentation, then the default indexes like the Geospatial index(which is on the 
	development server). Please make sure that Tomcat has at least 1.5 gig of memory, for large indexes this can go up to 8 gig depending 
	on configuration.<br><br>
			
	You can go to <a href="http://localhost:8080/ikube">user interface</a> to monitor how the indexes are coming along.<br><br> 
			 
	The default wait period for the scheduler to start is generally around five minutes. Of course you can change this we'll get into this in 
	the configuration section.<br><br>
	
	Note that all the integration configurations are in the configuration folder. Most of these can be deleted and or modified to suit your needs.
			 
</div>