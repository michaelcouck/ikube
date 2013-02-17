<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case eight: Index a database with the n-gram analyzer</h4>
	Index a database, with the n-gram analyzer for spelling tolerance and performance, perhaps auto complete for example:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/create-datasource.jsp" />
	<br><br>
	
	3) Create the database configuration: TODO Explain...
	<br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexDatabase" />
		</util:list>
		
		<bean
			id="myIndexDatabase"
			class="ikube.model.IndexableDataSource"
			property:name="myIndexDatabase"
			property:dataSource-ref="myIndexDatasource"
			property:excludedTablePatterns="SYS:$" />
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>