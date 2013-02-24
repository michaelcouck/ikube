<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case six: Geospatial database</h4>
	Index a database with geospatial data for geo-searching. <br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/create-datasource.jsp" />
	<br><br>
	
	3) Create the database configuration: First we create the children of the index, and add a table definition bean to the list. Then we create the table definition it's self, where the 
	geospatial data is. The name property of the table is actual name of the table in the database, this will b eused in generating the sql to access the data. 
	There is a full example in the configuration for reference if necessary, in the spring-geo.xml configuration file. We set the table to the primary table, and the address 
	to true although we don't need to use the address for the location because we have the co-ordinates of course. The only reason to have the address is because if the co-ordinates 
	are not available then we can define another strategy that will reverse geocode the address into a position using Google maps API or NavTec API or any other geocoding API that you have
	available..We set the datasource reference to your datasource of course, and the children to the latitude and longitude columns that we will define. We only have to define the two 
	columns, the other columns will be dynamically included in the index. We will store the data as usual, and analyze it and vector it as well. Now we define the geospatial strategy to enrich the 
	data during indexing. This strategy will extract the co-ordinates and produce a geohash from the position, then add it to the index to facilitate geopatial searches. <br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myIndexTableGeo" />
		</util:list>
		
		<bean
			id="myIndexTableGeo"
			class="ikube.model.IndexableTable"
			property:name="your-geospatial-data-table-name"
			property:primaryTable="true"
			property:address="true"
			property:dataSource-ref="myIndexDatasource"
			property:children-ref="myIndexTableColumnsGeo" 
			property:maxExceptions="10"
			property:allColumns="true"
			property:stored="true"
			property:analyzed="true"
			property:vectored="true"
			property:strategies-ref="myIndexTableGeoStrategies" />
	</textarea>
	<br><br>
	
	Next we create the children of the table, i.e. the columns. As mentioned we only have to define the latitude and longitude columns, the others will be included dynamically. We include the two 
	column definitions in the list of children for the table, and reference the list from the table definition. The name property of the column is the actual name of the column in the database. This will be 
	used to generate the sql to access the table.  
	We set the field name in the columns to latitude and longitude for convenience. Again we store, analyze and vector the contents. We specifically set the column to non numeric as we will be using the 
	field for geo-hashing. Add the snippit below to your configuration file under the table definition.
	
	<br><br>
	<textarea rows="15" cols="30">
		<util:list id="myIndexTableColumnsGeo">
			<ref local="myIndexTableLatitude" />
			<ref local="myIndexTableLongitude" />
		</util:list>
		
		<bean
			id="myIndexTableLatitude"
			class="ikube.model.IndexableColumn"
			property:name="your-latitude-column-name"
			property:fieldName="latitude"
			property:analyzed="true"
			property:stored="true"
			property:vectored="true"
			property:numeric="false" />
		<bean
			id="myIndexTableLongitude"
			class="ikube.model.IndexableColumn"
			property:name="your-longitude-column-name"
			property:fieldName="longitude"
			property:analyzed="true"
			property:stored="true"
			property:vectored="true"
			property:numeric="false" />
	</textarea>
	<br><br>
	
	Next we create the strategy for enriching the data with the geohashes. This strategy will be called with each iteration of a row in the database, extracting the latitude and longitude data from 
	the columns, creating a geo-hash from the data and enriching the Lucene index, ready for geo-hashing. Copy the snuppit below under the column definitions in your configuration file. Depending on 
	the volume of your data the indexing could take several hours.  
	
	<br><br>
	<textarea rows="15" cols="30">
		<util:list id="myIndexTableGeoStrategies">
		    <bean class="ikube.index.handler.strategy.GeospatialEnrichmentStrategy" />
		</util:list>
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>