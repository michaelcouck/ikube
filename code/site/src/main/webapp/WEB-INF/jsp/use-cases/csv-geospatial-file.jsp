<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Use cases</h2>
	<h4>Use case five: A CSV file with gepsoatial data</h4>
	Index a geospatial CSV file:<br><br>

	<jsp:include page="/WEB-INF/jsp/use-cases/index-context.jsp" />
	<br><br>
	
	2) Create the CSV file configuration: As with all the other indexes we create the list of children for the index context, then add our csv file 
	bean to the list of children. We specify the names of the fields in the Lucene index, the path, the file name, length, and content. We don't unpack 
	compressed files, we store the data, analyze it and vector it. We don't need to exclude any patterns, the batch size is not necessary as the CSV file 
	handler is not multi threaded. The max read length is not really necessary either, the separator used in the file, very important of course. The 
	maximum exceptions that we sill tolerate before stopping can be quite high, and we'll specify Windows-1252 as the format as this covers most 
	characters as it turns out. Please note that if your file is indeed some other format then you need to specify it or the search will produce some unexpected 
	results. This is indeed an address indexable, i.e. has coordinates for positioning and/or has an address that can be reverse geocoded for the global position.<br><br>
	
	We now need to define the geospatial strategy. Strategies will be executed before and possibly after indexing a resource, like a line in the csv file. With the 
	GeospatialEnrichmentStrategy, the latitide and longitude will be found in the line based on the definition of the columns. The co-ordinate will be used to generate 
	a geohash which will be added to the index. This data will be used to find data in a particular area, and indeed to sore the results from the point of origin. We add 
	the geospatial strategy to the list of strategies and reference it from the csv file dbean, see that in the streategy-ref property? So now the data will be enriched with 
	geospatial information during indexing.<br><br>
	
	We define the columns that have the longitude and latitude values in them so the strategy knows where to get the values from . Note that the values must be in radians 
	not degrees format. In the columns we define the names of the fields for latitude and longitude, and again we store the data, and analyze it and vector it, but not as a numeric 
	field. We soecify that this is not part of the address it's self.<br><br>
	
	Take the snippit below and copy it under the myIndex bean.
	<br><br>
	
	<textarea rows="15" cols="30">
		<util:list id="myIndexChildren">
			<ref local="myCsvFile" />
		</util:list>
		
		<bean
			id="myCsvFile"
			class="ikube.model.IndexableFileSystemCsv"
			property:name="myCsvFile"
			property:path="/media/nas/xfs-five/geoname/geoname.csv"
			property:pathFieldName="path"
			property:nameFieldName="name"
			property:lengthFieldName="length"
			property:contentFieldName="contents"
			property:unpackZips="false"
			property:stored="true"
			property:analyzed="true"
			property:vectored="true"
			property:excludedPattern="none"
			property:lastModifiedFieldName="lastmodified"
			property:batchSize="1000"
			property:maxReadLength="1000000"  
			property:separator=";" 
			property:maxExceptions="10000"
			property:encoding="Windows-1252"
			property:address="true" 
			property:strategy-ref="csvStrategies"
			property:children-ref="myCsvFileColumns" />
		<util:list id="myCsvFileColumns">
			<ref local="csvLatitude" />
			<ref local="csvLongitude" />
		</util:list>
		
		<util:list id="csvStrategies">
			<bean class="ikube.index.handler.strategy.GeospatialEnrichmentStrategy" />
		</util:list>
		
		<bean
			id="csvLatitude"
			class="ikube.model.IndexableColumn"
			property:name="csvLatitude"
			property:fieldName="latitude"
			property:analyzed="true"
			property:stored="true"
			property:vectored="true"
			property:numeric="false"
			property:address="false" />
		<bean
			id="csvLongitude"
			class="ikube.model.IndexableColumn"
			property:name="csvLongitude"
			property:fieldName="longitude"
			property:analyzed="true"
			property:stored="true"
			property:vectored="true"
			property:numeric="false"
			property:address="false" />
	</textarea>
	<br><br>
	
	<jsp:include page="/WEB-INF/jsp/use-cases/add-config.jsp" />
	
</div>