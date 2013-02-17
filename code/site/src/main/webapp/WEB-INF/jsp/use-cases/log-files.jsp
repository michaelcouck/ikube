<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case four: Log files</h4>
	Index a set of large log files for easily finding problems in production environments:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	2) Create the log file directory configuration: As usual we create  the children of the index context, and add the log file folder 
	to the list. This ensures that the log file folder will be processed by the log file handler when next scheduled. We set the path to the folder 
	where our log files are, the field name in the Lucene index for the file name, the path name, the line field name and the content field name, 
	i.e. the name of the field where the contents of the lines will go. We don't need to unpack zip files, we will store the contents, analyze it, vector it. 
	The batch size is not necessary as the log file handlers are single threaded, and we are assuming that each line in the log file will be less than a meg 
	in length, and we'll limit the exception tolerance to 100.<br><br>   
	
	Take the snippit below and copy it under the myIndex bean.
	<br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexFolderLog" />
		</util:list>
		
		<bean
			id="myIndexFolderLog"
			class="ikube.model.IndexableFileSystemLog"
			property:name="myIndexFolderLog"
			property:path="/path/to/log/files"
			property:fileFieldName="file"
			property:pathFieldName="path"
			property:lineFieldName="line"
			property:contentFieldName="contents"
			property:unpackZips="false"
			property:stored="true"
			property:analyzed="true"
			property:vectored="true" 
			property:batchSize="1000" 
			property:maxReadLength="1000000" 
			property:maxExceptions="100" />
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>