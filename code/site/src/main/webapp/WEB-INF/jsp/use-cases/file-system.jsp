<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case one: The file system</h4>
	Index a set of files on the file system. This can be the full hard disk of the machine, a remote file share or just a specific directory, or a set of directories:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jspf" />
	<br><br>
	
	2) Create the directory configuration: We first create the 'children' of the index, and then the directory 'bean', in this case there will be the only child of the index. The list of children 
	has one reference to the file system bean. And the index context above has a reference to the list of children, see it? In the file system bean, the path is the path on the file system where Ikube will 
	start reading the files off the file system. The path field name and the other field names are the names that Lucene will use when creating the index, and indeed this is what you 
	will use when you do a search on this document set. We will not unpack the zips, we will store the data to create fragments during the search, we will analyze it using Lucene, vector 
	the data too. We exclude 'sys' file pattern because system files are probably not interesting in binary form. The batch size is how many files each thread will process at a time. The 
	maximum read length is a meg, generally this is enough, and we set the maximum errors before we stop processing. Errors can happen when there is a badly formatted or corrupt file, 
	or in the case of Linux this could be non readable files due to permissions, etc. Take the snippit below and copy it under the myIndex bean.
	<br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexFolder" />
		</util:list>
		
		<bean
			id="myIndexFolder"
			class="ikube.model.IndexableFileSystem"
			property:name="myIndexFolder"
			property:path="/tmp"
			property:pathFieldName="path"
			property:nameFieldName="name"
			property:lengthFieldName="length"
			property:contentFieldName="contents"
			property:lastModifiedFieldName="lastmodified"
			property:unpackZips="false"
			property:stored="true"
			property:analyzed="true"
			property:vectored="true"
			property:excludedPattern=".*(sys).*"
			property:batchSize="1000"
			property:maxReadLength="1000000" 
			property:maxExceptions="100" />
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jspf" />
	
</div>