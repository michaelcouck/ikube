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
			2) spring-beans.xml - application beans, i.e. not really for clients to modify<br>
			3) spring-jdbc.xml - for datasources that need to be indexed, for clients<br>
			4) spring-client.xml - this is the interesting one at the moment, for clients<br><br>
			
			For the first install we will use the default configuration in the war. For the install you need to down-load your favorite version 
			of Tomcat and pop the <a href="http://code.google.com/p/ikube/downloads/list" target="_top">war</a> in the webapps directory. 
			Then press start. Wait...<br><br>
			Wait...<br><br>
			Wait...<br><br>
			Done.<br><br>
			
			In the default configuration there are three(could change over time of course) indexes defined. They all have a url(site), a file 
			share, a mail account and a database to index. The database index will fail because the url in the configuration is for my 
			machine, no worries. The file share likewise but the url(which is the Ikokoon site) will succeed as will the mail account.<br><br>
			
			You will see in the ${TOMCAT_INSTALL_DIR}/bin that there is a folder 'indexes' and in here there are three indexes, indexOne, indexTwo and 
			you guessed it indexThree. You want to search this index then. Well there is a web service that will be deployed to 
			http://169.254.107.201:8081/ikube/service/ISearcherWebService?wsdl. Npte that the ip address is the one that is returned by 
			InetAddress.getLocalHost().getHostAddress(). Typically this is the auto-configuration IPV4 address. The service will not bind to 
			'localhost' generally, and it is not the network address like 192.168.0.1 either. Just to make things more interesting of course. The 
			reason for this is that you could run several Tomcats in a cluster on the same machine and the ip addresses must not clash nor on the network 
			in a cluster.<br><br>
			
			You could write a client or use a tool like SoapUI to 
			access the results or you can use the <a href="http://code.google.com/p/ikube-client/" target="_top">Ikube Client</a>. We'll assume that you 
			will use the client. So unpack the zip and double click the ikube.exe file. It will connect to the Hazelcast cluster and display the server. Double 
			click on the server and the details page will open. Select the index from the drop down. Type ikokoon in the 'content' field and click the 
			search button. Violla.<br><br>
			
			Here is a screen shot of the client with the default index:<br><br>
			
			In this configuration the configuration files were in the war. The default is first to look a folder 'ikube' in the folder where the server 
			was started, i.e. in the bin folder(${TOMCAT_INSTALL}/bin/ikube). If there are no files in this folder then the files in the war will be used 
			for configuration. Generally clients would want to have their configuration out side the war. On the other hand having the configuration in the 
			war will greatly simplify the deployment in a clustered environment. For example in JBoss just drop the war into the 'farm' folder and JBoss will 
			propergate the deployment into the cluster. Outside the war, each server will have to be configured separately, updating the configuration 
			will require that each server is again configured. You get the picture? 
			To try an external configuration follow the instructions below.<br><br> 
			
			There is a minimal configuration set in the project, this can be found in the  
			<a href="http://code.google.com/p/ikube/source/browse/#svn%2Ftrunk%2Fmodules%2Fcore%2Fsrc%2Fmain%2Fresources%2FMETA-INF%2Fminimal"
				target="_top">minimal</a> 
			folder. This folder contains the base beans for the application and one client configuration file, the only file that is 
			interesting at the moment. In the spring-client.xml there is an index defined and a url to index. As it turns out it is the 
			Ikokoon site, mine. 46 pages in various languages, perfect for a first exercise.<br><br>
			
			Get the files in this minimal configuration folder and copy the files to your Tomcat ${TOMCAT_INSTALL}/bin/ikube directory. 
			The application will look for the spring.xml file in the working directory under 'ikube', i.e. where the Jvm 
			was started. In the case of a Tomcat that will be in the 'bin/ikube' directory, where the startup.bat is. Re-start Tomcat.<br><br> 
			
			Re-start the client. As it turns out the client is not fully implemented at the time of writing and there is no update 
			mechanism for servers that are closed or no longer reachable in the cluster. Of course this will be remedied in the not too 
			distant future.	<br><br>
			
			That is it. Wait for a while and the indexing will start. In a few seconds it will finish, and violla. Ready to search...<br><br>
			
			Have a fantastic day!
		</td>
	</tr>
</table>