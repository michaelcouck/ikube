<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">quick start</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td>
			<strong>configuration</strong>&nbsp;
			The first thing anyone wants to do is index a web site. The configuration is split into separate files, of course they 
			can be in one file, it is just easier to have the database configuration separate from the internet configuration. For 
			now we are not interested in the database setup because we are just going to do one web site. For completeness 
			the files of interest in the default configurations are as follows: 
			<br><br>
			
			Mandatory system configuration:<br>
			1) spring.xml - contains includes of the other configuration files<br>
			2) spring-beans.xml - application beans, i.e. not really for clients to modify<br>
			3) spring-actions.xml - the indexing actions defined for Ikube<br>
			4) spring-aop.xml - the AOP file for the rules engine and monitoring<br><br>
			
			Optional system configuration:<br>
			5) spring-integration.xml - the integration testing beans<br>
			6) spring-ikube.xml - and the configuration to index the documentation in the WAR<br>
			7) spring-geospatial.xml - the index for the geospatial index. This index is for clients 
				that have address data and need to order the results wy distance from a 
				point(latitude and longitude)<br><br>
			
			Optional client configuration:<br>
			8) spring-client.xml - this is the interesting one at the moment, for clients<br>
			9) spring-db2-jdbc.xml - the Db2 configuration<br>
			10) spring-h2-jdbc.xml - the H2 datasource configuration<br>
			11) spring-oracle-jdbc.xml - the Oracle datasource configuration<br><br>
			
			For the first install we will use the default configuration in the war. For the install you need to down-load your favorite version 
			of Tomcat and pop the <a href="http://code.google.com/p/ikube/downloads/list" target="_top">war</a> in the webapps directory. 
			Then press start. Alternatively there could be a fully functional cluster with everything in a zip in the Google Code download page 
			that you can just unpack and start one by one, if you want to check the cluster functionality. Remember that each instance of Ikube 
			needs around 1 gig.<br><br>
			 
			Wait...<br><br>
			Wait...<br><br>
			Wait...<br><br>
			Done.<br><br>
			
			The default wait period for the scheduler to start is generally around five minutes. Of course you can change this by opening the 
			war in WinRar and modifying the property in the spring.properties 'delay' in the WEB-INF/common folder, or putting the configuration 
			outside the server, we'll get into this in the configuration section.<br>
		</td>
	</tr>
	<tr>
		<td>
			In the default configuration there are three(could change over time of course) indexes defined. They all have a url(site), a file 
			share, a mail account and a database to index. The database index will fail because the url in the configuration is for my 
			machine, no worries. The file share likewise but the url(which is the ikube site on CloudBees) will succeed as will the mail account.<br><br>
			
			You will see in D:/cluster that there is a folder 'indexes' and in here are the indexes, the default indexes are 'index', 'ikube' and 
			'geospatial'. Generally though these indexes will have thrown exceptions and there will be no data in them. But the ikube index 
			should have some data in it, provided the port Tomcat is running on is either 80 or 9000. 
			You want to search this index then. Well there is a web service that will be deployed to 
			http://yourip:8081/ikube/service/ISearcherWebService?wsdl. Note that the ip address is the one that is returned by 
			InetAddress.getLocalHost().getHostAddress(). Typically this is the auto-configuration IPV4 address. The service will not bind to 
			'localhost' generally, and it is not the network address like 192.168.1.101 either. Just to make things more interesting of course. The 
			reason for this is that you could run several Tomcats in a cluster on the same machine and the ip addresses must not clash nor on 
			the network in a cluster. If you can't find the web service then go to the ikube.log file and look for 'Publishing web service' and the url 
			will be there, along with the port and ip.<br><br>
			
			You could write a client or use a tool like SoapUI to 
			access the results or you can use the <a href="<c:url value="/admin/servers.html" />" target="_top">Ikube Client</a>. We'll 
			assume that you will use the client. On this page is the servers that are connected to Ikube cluster. Below the servers' details are 
			the indexes' details. Clicking on an index, like the Ikube index, will take you to the search page. What will pop up are the fields in 
			the index and text fields on the page where you can input text, either individually per field or combinations of input strings. Searching 
			for 'Ikube' in the 'content' field will generally result in several results.
			<br><br>
		</td>
	</tr>
	<tr>
		<td>
			The configuration for this instance is in the ikube-{version}.war, inside the ikube.jar in the META-INF directory. Generally this is convenient 
			because you just need to drop the war in the server and off it goes, and in a cluster you only need one file, i.e. the war. The down side 
			is that you need to change the configuration in the war/WEB-INF/lib/ikube-core.jar/META-INF/common/spring.properties file. Ikube is based 
			on Spring, and the configuration relies on the configuration files being on the classpath, so this is un-avoidable.<br><br>
			
			There is a Tomcat with everything set up, all you need to do is download it and unpack it, then you can change the configuration details 
			as you wish, from <a href="http://code.google.com/p/ikube/downloads/list">here</a>, there should be a file complete.zip, or 
			something like it.<br><br>
			 
			Start Tomcat. That is it. Wait for a while and the indexing will start. Generally the delay for the scheduler is set to a few 
			minutes but you can change this, it is in the properties file described above, change the 'delay' property, change it to 10000 
			and the indexer will start in ten seconds, or just go have a cup of coffee and read the paper. Then... ready to search... You 
			can go back to the monitoring page and go to the search page from there for the server of your choice.<br><br>
			
			Have a fantastic day! 
			Index the planet!
		</td>
	</tr>
</table>