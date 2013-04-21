<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table width="100%">
	<tr><td><span style="float: right;"><script type="text/javascript">writeDate();</script></span></td></tr>
	<tr>
		<td>
			<strong>clustering</strong>&nbsp;
			Ikube will automatically cluster them selves via UDP broadcasting. Whether it is installed in a server, a 
			cluster of servers or stand alone. Provided the machines and the Jvms are reachable through the firewall. Each instance is 
			multi threaded, however there is no load distribution during indexing. Each instance will index one defined index completely. For 
			large indexes the data must be partitioned statically, i.e. within the definition of the index. This is described later.
		</td>
	</tr>
	<tr>
		<td>
			There is no difference between configuration for Ikube stand alone and in a cluster. However the location 
			of the indexes, i.e. where they will be written by each instance can either be defined as a relative directory 
			meaning that the instance will write the index to the local file system. Or it can be defined as an absolute path 
			on the network, where all the instances will write their indexes. In the first case each instance will perform the 
			indexing on every index, which is undesirable. In the latter case only one instance will index the data and all the 
			instances will use the same index. 
		</td>
	</tr>
	<tr>
		<td>
			For very large data sets(1 000 000 000) documents++, it is advisable that the load get statically distributed over the 
			cluster using the configuration to partition the data into segments per server. For example if you have one billion documents 
			spread out over 10 file servers, roughly evenly, then you should create and index per file server, rather than putting all the 
			file servers in the same index. This way each Ikube instance will index one file server, in parallel, and the load will be distributed 
			evenly over the cluster.
		</td>
	</tr>
	<tr>
		<td>
			One thing to note is that the disk io will probably be the bottleneck and not the cpu. If you have one billion documents on a single 
			disk then this will take a very long time to index indeed. To be able to index this volume you need to split the data on separate disks 
			and define multiple indexes, each with a single thread. Adding threads to the file system handlers does not increase the throughput 
			of the indexing process, indeed it will probably slow it down.
		</td>
	</tr>
	<tr>
		<td>
			Another thing this with large volumes is that on Windows, the operating system does not release resources efficiently, and the maximum 
			I found on Server 2008 EE was around 8 million before the machine (Dell Rack) ran out of memory. I have not performed volume tests 
			on Windows environments as most if not all production systems will be Linux, and d indeed it is not possible it seems, from within Java at least 
			to have large indexes generated on Windows.
		</td>
	</tr>
</table>