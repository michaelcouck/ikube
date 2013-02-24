<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case eight: The file system, only the deltas</h4>
	Index the file system, but only the changes in the data/documents: This configuration is reliant on the underlying operating system, and indeed the file system, 
	to provide accurate dates for changes and file length. The process for the deltas is as follows:<br><br>
	
	1) Pre-processing involves getting the hash from the index for all the files<br>
	2) During processing, calculate the hash for the file based on the length and the last modified time<br>
	3) Check the hash against the hashes in the index, if it exists then don't process the file and remove the hash from the index, if it doesn't then add the file to the index<br>
	4) Post-processing involves removing all the files from the index where the hash still exists in the index but there is no file, i.e. removed files<br><br>
	
	This process is memory and cpu intensive, and generally not recommended for critical applications as the remove relies on Lucene to delete documents based on a query and 
	not on the unique id in the index which can result in strange results. Having said that it is the default for the desktop search.
	<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	2) Create the directory configuration: First change the delta property in the index context to true, 'property:delta="true"'. Then create the folder/file system that you would like to index. Add the 
	file system bean to the children of the index, i.e. to the myIndexChildren list. We define the path as the base of the disk, like the desktop folder for example. Also specify the path field name, the name 
	field name, the length and content field names. We won't unpack the compressed files for this one. And again stored, analyzed and vectored. Add some excluded patterns, like /mnt so Ikube doesn't 
	wonder off over the network, unless you want to of course. The batch size of the files per thread, not critical, but 1000 is a good number, and we'll allow 100 exceptions, we should allow more for a 
	typical file system, but we won't for now. Implrtantly we add the delta strategy which will provide the logic to add the file as changed, and remove the old one if necessary.<br><br>
	
	Take the snippit below and copy it under the myIndex bean.
	<br><br>
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexFolder" />
		</util:list>
		
		<bean
			id="myIndexFolder"
			class="ikube.model.IndexableFileSystem"
			property:name="myIndexFolder"
			property:path="/"
			property:pathFieldName="path"
			property:nameFieldName="name"
			property:lengthFieldName="length"
			property:contentFieldName="contents"
			property:unpackZips="false"
			property:stored="true"
			property:analyzed="true"
			property:vectored="true"
			property:excludedPattern=".*(.svn).*|.*(.db).*|.*(.exe).*|.*(.dll).*|.*(Password).*|.*(password).*|.*(RSA).*|.*(MANIFEST).*|.*(root).*|.*(/proc).*|.*(/bin).*|.*(/boot).*|.*(/sbin).*|.*(/sys).*|.*(/media).*|.*(/mnt).*|.*(Backup).*|.*(/sys).*"
			property:lastModifiedFieldName="lastmodified"
			property:batchSize="1000"
			property:maxReadLength="1000000" 
			property:strategies-ref="myDeltaStrategies"
			property:maxExceptions="100" />
		
		<util:list id="myDeltaStrategies">
		    <bean class="ikube.index.handler.strategy.DeltaIndexableFilesystemStrategy" />
		</util:list>
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>