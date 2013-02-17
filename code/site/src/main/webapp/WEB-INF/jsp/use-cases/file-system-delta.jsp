<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case seven: The file system, only the deltas</h4>
	Index the file system, but only the changes in the data:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	2) Create the directory configuration: TODO Explain...
	
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
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>