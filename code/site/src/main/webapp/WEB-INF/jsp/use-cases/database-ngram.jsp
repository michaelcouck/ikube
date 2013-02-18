<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case seven: Index a database with the n-gram analyzer</h4>
	Index a database, with the n-gram analyzer for spelling tolerance and performance, perhaps auto complete for example: <br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/create-datasource.jsp" />
	<br><br>
	
	3) Create the database configuration: The n-gram analyze needs to be added to the index context bean. Please add the snippit as a property on the myIndex 
	bean - 'property:analyzer-ref="myNgramAnalyzer"'. The reason for this is because if an index is created with an analyzer, the searcher needs to use 
	the same analyzer, and the searcher for the index is at the top level bean, i.e. the index context bean. And we define the analyzer for the index, please copy and paste 
	the definition of the analyzer into the configuration file.<br><br>
	
	Next we define the database, and indeed include all the tables, and all the columns dynamically. You can reduce the ammount of tables and columns, and define them seperately 
	of course buy specifying the tables one by one, and the columns one by one, and indeed this is probably what a production configuration would be. Typically you would not want to index 
	all the data in all the tables, but only specific columns. But for the sake of brevity we take the short cut.<br><br>
	
	The data will now be broken into n-grams before being added to the index. This increases the size of the index but allows for very fast 'fuzzy' search and allows fault tolerance 
	for spelling mistakes. This is good for autocomplete tables for example where the user may type in the wrong spelling of the words, but will still match some tri-grams resulting in 
	some hits for the sentence. Processing n-grams is also much more cpu intensive, this should be borne in mind when analyzing the volumes of data that are to be processed.
	
	<br><br>
	<textarea rows="15" cols="30">
		<bean 
			id="myNgramAnalyzer" 
			class="ikube.index.analyzer.NgramAnalyzer" />
		
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