<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">configuration</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<strong>file system</strong>&nbsp;
			This configuration section is to index a file system. Could be on the local machine or across the network. It could be on Windows or Linux, it 
			makes no difference. Before going through the configuration options, please make sure you have an instance of Tomcat running with an instance 
			of Ikube in it by refering to the quick start at <a href="<c:url value="/documentation/configuration.html" />" >quick start</a>.
		</td>
	</tr>
	<tr>
		<td colspan="2">
			
		</td>
	</tr>
	<tr>
		<td colspan="2">
			
 		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<th colspan="2">Indexable file system definition parameters</th>
	</tr>
	<tr>
		<td colspan="2">
			This indexable definition is for a file share. It can be on the local machine or on the network. For a local directory 
			the path to the folder will be /path/to/folder and on the network would be something like //computer.name/path/to/folder.
		</td>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td>
			The uniqiue name of this indexable in the configuration.
		</td>
	</tr>
	<tr>
		<td>path</td>
		<td>
			The absolute or relative path to the file or folder to index. This can be accross the network 
			provided the drive is mapped to the machine where Ikube is running.
		</td>
	</tr>
	<tr>
		<td>pathFieldName</td>
		<td>
			The name in the Lucene index of the path to the file that is being indexed.
		</td>
	</tr>
	<tr>
		<td>nameFieldName</td>
		<td>
			The name in the Lucene index of the name of the file.
		</td>
	</tr>
	<tr>
		<td>lengthFieldname</td>
		<td>
			The name of the field in the Lucene index of the length of the file, ie. the size of it. 
		</td>
	</tr>
	<tr>
		<td>contentFieldName</td>
		<td>
			The name of the field in the Lucene index for the fiel content. This is typically the important field 
			that will be searched once the index is created.
		</td>
	</tr>
	<tr>
		<td>excludedPattern</td>
		<td>
			Any excluded patterns that would be excluded from the indexing process, like for example 
			exe files and video as Ikube can't index video just yet, although there is some investigation into 
			this at the moment.
		</td>
	</tr>
	<tr>
		<td>lastModifiedFieldName</td>
		<td>
			The name of the field in the Lucene index for the last modified timestamp of the file being indexed.
		</td>
	</tr>
</table>
