<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">quick start</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>configuration</strong>&nbsp;
			The first thing anyone wants to do is index a web site. The configuration is split into separate files, of course they 
			can be in one file, it is just easier to have the database configuration separate from the internet configuration. For 
			now we are not interested in the database setup because we are just going to do one web site. For completeness 
			the files of interest in the default configurations are as follows: 
			<br><br>
			1) spring.xml - contains includes of the other configuration files<br>
			2) spring-db2-jdbc.xml - the Db2 configuration<br>
			3) spring-h2-jdbc.xml - the H2 datasource configuration<br>
			4) spring-oracle-jdbc.xml - the Oracle datasource configuration<br>
			5) spring-client.xml - this is the interesting one at the moment, for clients<br>
			6) spring-integrration.xml - the integration testing beans<br>
			7) spring-actions.xml - the indexing actions defined for Ikube<br>
			8) spring-aop.xml - the AOP file for the rules engine and monitoring<br>
			9) spring-beans.xml - application beans, i.e. not really for clients to modify<br>
			10) spring-ikube.xml - and the configuration to index the documentation in the WAR<br><br>
			
			For the first install we will use the default configuration in the war. For the install you need to down-load your favorite version 
			of Tomcat and pop the <a href="http://code.google.com/p/ikube/downloads/list" target="_top">war</a> in the webapps directory. 
			Then press start.<br> 
			Wait...<br><br>
			Wait...<br><br>
			Wait...<br><br>
			Done.<br><br>
			
			The default wait period for the scheduler to start is generally around five minutes. Of course you can change this by opening the 
			war in WinRar and modifying the property in the spring.properties 'delay' in the WEB-INF/common folder.<br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			In the default configuration there are three(could change over time of course) indexes defined. They all have a url(site), a file 
			share, a mail account and a database to index. The database index will fail because the url in the configuration is for my 
			machine, no worries. The file share likewise but the url(which is the ikube site on CloudBees) will succeed as will the mail account.<br><br>
			
			You will see in the ${TOMCAT_INSTALL_DIR}/bin that there is a folder 'indexes' and in here there are indexes. 
			You want to search this index then. Well there is a web service that will be deployed to 
			http://169.254.107.201:8081/ikube/service/ISearcherWebService?wsdl. Note that the ip address is the one that is returned by 
			InetAddress.getLocalHost().getHostAddress(). Typically this is the auto-configuration IPV4 address. The service will not bind to 
			'localhost' generally, and it is not the network address like 192.168.0.1 either. Just to make things more interesting of course. The 
			reason for this is that you could run several Tomcats in a cluster on the same machine and the ip addresses must not clash nor on 
			the network in a cluster.<br><br>
			
			You could write a client or use a tool like SoapUI to 
			access the results or you can use the <a href="http://localhost:9000/ikube/admin/servers.html" target="_top">Ikube Client</a>. We'll 
			assume that you 
			will use the client. On this page is the servers that are connected to Ikube cluster. There is a details link to have a look at 
			the details of the server and a link to search indexes. Clicking on this link goes to the search page for indexes for this server. 
			There should always be at least one index, the documentation index of Ikube. Do a quick search on this index for Ikube should 
			result in a couple of results.<br><br>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			In this configuration the configuration files were in the war. The default is first to look a folder 'ikube' in the folder where the server 
			was started, i.e. in the bin folder(${TOMCAT_INSTALL}/bin/ikube). If there are no files in this folder then the files in the war will be used 
			for configuration. Generally clients would want to have their configuration out side the war. On the other hand having the configuration in the 
			war will greatly simplify the deployment in a clustered environment. For example in JBoss just drop the war into the 'farm' folder and JBoss will 
			propagate the deployment into the cluster. Outside the war, each server will have to be configured separately, updating the configuration 
			will require that each server is again configured. You get the picture? 
			To try an external configuration follow the instructions below.<br><br> 
			
			There is a minimal configuration set in the project, this can be found in the  
			<a href="http://code.google.com/p/ikube/source/browse/#svn%2Ftrunk%2Fcode%2Fcore%2Fsrc%2Fmain%2Fresources%2FMETA-INF%2Fclient"
				target="_top">minimal</a> 
			folder. This folder contains the base beans for the application and client configuration files, the only file that is 
			interesting at the moment is the spring-minimal.xml.<br><br>
			
			Right, so you can get all the configuration files from here and copy them to your ${TOMCAT_INSTALL}/bin/ikube directory, or 
			you can get all the configuration files from the WAR. They will be in the META-INF directory. Copy all the files from here to the 
			${TOMCAT_INSTALL}/bin/ikube directory. There will be quite a few, all the files mentioned above, and keep the directory 
			structure. Now open the spring.xml file. Change the import from<br><br>
			 
			* classpath:/META-INF/client/spring-client.xml<br>
			To<br>
			* classpath:/META-INF/client/spring-minimal.xml<br><br>
			
			The application will look for the spring.xml file in the working directory under 'ikube', i.e. where the Jvm 
			was started. In the case of a Tomcat that will be in the 'bin/ikube' directory, where the startup.bat is. Re-start Tomcat.<br><br> 
			
			That is it. Wait for a while and the indexing will start. Generally the delay for the scheduler is set to a few minutes but you 
			can change this, it is in the bin/ikube/common/spring.properties, the 'delay' property, change it to 10000 and the indexer 
			will start in ten seconds, or just go have a cup of coffee and read the paper. Then... ready to search... You can go back to 
			the monitoring page and go to the search page from there for the server of your choice.<br><br>
			
			Have a fantastic day! Index the planet!
		</td>
	</tr>
</table>