<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table width="100%">
	<tr><td><span style="float: right;"><script type="text/javascript">writeDate();</script></span></td></tr>
	<tr>
		<td>
			<strong>installation</strong>&nbsp; Seven steps:<br><br>
			
			1) Down-load the latest version at <a href="http://code.google.com/p/ikube/downloads/list" target="_top">war</a>.<br>
			2) Down-load the acompanying Tomcat <a href="http://code.google.com/p/ikube/downloads/list" target="_top">Tomcat</a>.<br>
			3) Copy the war to the webapps directory of Tomcat.<br>
			4) The default configuration is already in the bin directory, so don't worry abou that<br>
			5) Change the name of the war to ikube.war if not already so.<br>
			6) Start Tomcat. <br>
			7) Have a cup of coffee.<br><br>
			
			That's all. Ikube will start indexing first it's own documentation, then the default indexes like the Geospatial index(which is on the 
			Ikube server). Please make sure that Tomcat has at least 1.5 gig of memory, for 
			large indexes this can go up to 4 gig depending on configuration.<br><br>
			
			You can go to <a href="http://localhost:8080/ikube">Ikube</a> to monitor how the indexes are coming along. There is also 
			a search page that will search all the fields in all the indexes. By searching for 'university AND of AND cape AND town AND NOT ikube' you should get some 
			results from the Geospatial index at the very least.<br><br> 
			 
			The default wait period for the scheduler to start is generally around five minutes. Of course you can change this we'll get into this in 
			the configuration section.<br><br>
		</td>
	</tr>			
</table>