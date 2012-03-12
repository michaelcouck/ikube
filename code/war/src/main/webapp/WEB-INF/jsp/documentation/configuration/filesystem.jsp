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
			makes no difference. 
			
			For a local directory the path to the folder will be /path/to/folder and on the network would be something like //computer.name/path/to/folder 
			depending on your favourite operating system. 
			
			Before going through the configuration options, please make sure you have an instance of Tomcat running with an instance 
			of Ikube in it by refering to the quick start at <a href="<c:url value="/documentation/quickstart.html" />" >quick start</a>.
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
			The absolute or relative path to the file or folder to index. If the path is relative to the starting directory of the server, Tomcat for example, 
			then the path should start with a point, './path/to/index'. Generally though the path to the directory that is to be indexed will be absolute, for 
			example '/usr/local/documentation'. This can be accross the network provided the drive is mapped to the machine where Ikube is running.
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
	<tr>
		<td>maxReadLength</td>
		<td>
			the maximum length of any file to read before passing the data to the parsers. Unfortunately Word and PDF files require 
			that the entire document is read into memory before being analyzed. As a result this property will limit files of 100 meg being 
			read into memory.
		</td>
	</tr>
	
	<tr>
		<td>unpackZips</td>
		<td>
			This flag is to indicate whether zips and other compressed files should be opened and the contents indexed as well. Each file 
			in the archive will be treated as a separate file, and will not be added the the archive's data. Note that this is not a good idea on 
			Windows due to some strange behaviour while opening encrypted files in jars. The handler can become stuck. This problem is not 
			experienced on Linux and Mac however. Note that the archives are not really unpacked, the data is read inside the archives.
		</td>
	</tr>
	<tr>
		<td>analyzed</td>
		<td> 
			A Lucene parameter, whether the data should be analyzed. Generally this is true.
		</td>
	</tr>
	<tr>
		<td>stored</td>
		<td> 
			A Lucene parameter, whether the data should be stored in the index. Generally this is true. Of course if there is very large volumes of data 
			then storing the data could be prohibitively expensive, in terms of disk space and time. The write time of the index is a large proportion of the 
			indexing time.   
		</td>
	</tr>
	<tr>
		<td>vectored</td>
		<td> 
			A Lucene parameter, whether the data should vectored. Generally this is true.
		</td>
	</tr>
	
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	
</table>