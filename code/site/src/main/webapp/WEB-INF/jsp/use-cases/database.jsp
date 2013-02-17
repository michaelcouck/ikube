<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case two: Index a database</h4>
	Index a database, all the tables, paying no attention to the names of the tables or the columns:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/create-datasource.jsp" />
	<br><br>
	
	3) Create the database configuration: We have defined the datasource, now we will create the bean that will index the datasource. First the children of the myIndex bean, then a reference to the 
	data source bean. There is a reference from 'myIndexDatabase' to 'myIndexDatasource', see it? When the inxeding starts, then the database bean will use the data source bean to get a connection to 
	the database, list all the tables and index them one at a time, every column in every table. Copy the snippit below and paste it into your configuration file below the 'myIndexDatasource' bean but above 
	the end 'beans' tag. 
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