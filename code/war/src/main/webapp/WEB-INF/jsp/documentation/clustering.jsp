<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">clustering</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			<strong>clustering</strong>&nbsp;
			Ikube will automatically cluster them selves via UDP broadcasting. Whether it is installed in a server, a 
			cluster of servers or stand alone. Provided the machines and the Jvms are reachable through the firewall there 
			will be automatic load distribution during the indexing. This facilitates large volumes, and in fact it is not 
			recommended to use Ikube for anything less than 100m records of any kind due to the overhead 
			of the configuration which is not trivial. There are other much simpler index engines available that are 
			considerably easier to configure and deploy. Having said that few if any at all have the capability to index
			data in complex hierarchical structures in databases. 
		</td>
	</tr>
	<tr>
		<td class="td-content" colspan="2">
			There is no difference between configuration for Ikube stand alone and in a cluster. However the location 
			of the indexes, specifically the index directory path('index.directory.path' in the client spring.properties file) must 
			be on the network as all the servers in the cluster will write to the same index directory and will then open 
			the index searcher on that directory when the index is finished. So all that needs to be done is to set up 
			the indexables to index(database, internet, file system etc) and drop the war into the server deploy directory 
			or deploy as instructed by your server documentation.
		</td>
	</tr>
</table>
