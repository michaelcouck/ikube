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
</table>