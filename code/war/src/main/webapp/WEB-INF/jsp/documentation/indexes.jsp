<%@ page errorPage="/WEB-INF/jsp/error.jsp" contentType="text/html" %>
<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">indexes</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>indexes</strong>&nbsp;
			Indexes are created and written to the path on the network specified by the index path in the configuration. When 
			Ikube is deployed in a cluster, each server will write it's index to the same base directory, and subsequently open it's index 
			and the indexes of the other servers in this directory as well. As such for large indexes there needs to be enough disk 
			space on the machine where the index is being written.
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>stopping and starting</strong>&nbsp;
			Indexing can be stopped using the stop button on the monitoring page. This will terminate all threads, and indeed the pool of threads, immediately 
			teminating all indexing jobs abruptly. This can be usefull fo various reasons, like for example the index disk running out of space, incorrect index definitions 
			and so on.<br>
			To start the thread pool again click on the start button. This will set in motion all the events and the threads to start indexing processes and so on.
		</td>
	</tr>
</table>