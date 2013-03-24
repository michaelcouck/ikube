<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="maincontent">
	<h2>Search API</h2>
	
			The access to search results is a via a rest web service. The formats are Xml and Json. These links are the Xml services, but for Json just 
			add json after the search in the path, i.e. http://ikube.be/ikube/service/search/json/single? . Method signatures for the web 
			service methods for searching are:
			<br><br>
			
			<c:url var="single" value="http://www.ikube.be:9000/ikube/service/search/single">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="cape AND town AND university" />
				<c:param name="searchFields" value="name" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
			</c:url>
			
			<c:url var="multi" value="http://www.ikube.be:9000/ikube/service/search/multi">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="university:south AND africa" />
				<c:param name="searchFields" value="name:country" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
			</c:url>
			
			<c:url var="multiAll" value="http://www.ikube.be:9000/ikube/service/search/multi/all">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="university:south AND africa" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
			</c:url>
			
			<c:url var="multiSorted" value="http://www.ikube.be:9000/ikube/service/search/multi/sorted">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="university:south AND africa" />
				<c:param name="searchFields" value="name:country" />
				<c:param name="sortFields" value="name:country" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
			</c:url>
			
			<c:url var="multiSpatial" value="http://www.ikube.be:9000/ikube/service/search/multi/spatial">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="cape AND town AND university" />
				<c:param name="searchFields" value="name" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
				<c:param name="distance" value="10" />
				<c:param name="latitude" value="-33.95796" />
				<c:param name="longitude" value="18.46082" />
			</c:url>
			
			<c:url var="multiSpatialAll" value="http://www.ikube.be:9000/ikube/service/search/multi/spatial/all">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="cape AND town AND university" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
				<c:param name="distance" value="10" />
				<c:param name="latitude" value="-33.95796" />
				<c:param name="longitude" value="18.46082" />
			</c:url>
			
			<c:url var="complex" value="http://www.ikube.be:9000/ikube/service/search/complex">
				<c:param name="indexName" value="geospatial" />
				<c:param name="searchStrings" value="cape|town" />
				<c:param name="searchFields" value="name|name" />
				<c:param name="typeFields" value="string|string" />
				<c:param name="fragment" value="true" />
				<c:param name="firstResult" value="0" />
				<c:param name="maxResults" value="10" />
			</c:url>
			
			1) Search in a single field in the index - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${single}" />', 'Single Field Search');">single field</a><br>
			2) Search multiple fields in the index - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${multi}" />', 'Multiple Field Search');">multiple fields</a><br>
			3) Search all the fields in the index - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${multiAll}" />', 'Multi All Field Search');">multiple strings, all fields</a><br>
			4) Search all the fields in the index and sort them according to the sort fields - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${multiSorted}" />', 'Multi Sorted Field Search');">multiple fields, sorted on two fields</a><br>
			5) Search a single field and sort the results according to distance from a point - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${multiSpatial}" />', 'Multi Spatial Field Search');">multiple fields, spatial</a><br>
			6) Search all the fields and sort the results according to distance from a point - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${multiSpatialAll}" />', 'Multi Spatial All Field Search');">multiple string, all fields, spatial</a><br>
			7) Complex search, using numeric, string and possibly ranges - 
				<a href="#" onclick="JavaScript:popup('<c:out value="${complex}" />', 'Complex search');">complex search on string fields</a><br>
			
			<br><br>
			<strong>Parameters for searching:</strong><br>
			1) Index name - The index name is the name of the index that you want to search.<br> 
			2) Search string(s) - The search string is the search string(s), which could have Lucene syntax, for example for wild card searches 
				and the like. Please refer to the <a href="http://lucene.apache.org/java/3_0_0/queryparsersyntax.html">Lucene syntax</a> 
				documentation for all the possibilities that you can use for searching indexes. Keep in mind that a wild card search will iterate through all 
				the documents fields' running a Levenshtein distance on the data, and will be very expensive. The search strings(if there are more than one) 
				are delimited by a semi-colon for example 'hotels;bed and breakfast'<br><br>
			3) Search field(s) - The search fields are the fields in the Lucene index that you want to search.<br>
			4) Sort field(s) - The sort fields are the fields that will be sorted on, in the order that they are specified.<br>
			5) Fragment - Whether to create a fragment from the data stored in the index. Note that fragments can only be generated 
				if the 'stored' parameter is specified in the Lucene configuration, i.e. that the data is in fact stored in the index as is.<br>
			6) First result - This is the first result as it appears in the list of results. This can be specified to allow paging, along with the 
				max results parameter.<br>
			7) Max results - This parameter will limit the results to a set, perhaps 10 or even 100, starting at the point in the results where 
				the first result parameter was specified.<br>
			8) Distance - The distance is for spatial searches. This defines the best fit distance that should be taken into consideration when 
				allocating the results from a point. The value of this should be from 1 up to 20 as 20 is the defined tiers' size during indexing. This 
				will become a property and can be modified by the user for different spatial indexes.
			9) Latitude and longitude - These are the points that will be used as the origin of the search, i.e. the starting point to find 
				results around. This will also be used to sort the results, radiating outward from this point.
			10) Type fields - This search parameter defines what type the field is to be searched. Fieldscan be of type numeric or string. In the case of a numeric field, 
				 the field can be searched with a number but interestingly also for ranges, which is not the case for string fields. Note that you have to define your field as 
				 numeric before indexing before you can search it as a numeric or range.
				<br><br>
				
			In all cases the result is a string. This string is a serialized list of maps(in xml). Each map is one result. A result consists of four fields:<br><br>
			
			1) index - the offset in the Lucene index of the Lucene document<br>
			2) all fields - the fields and their content in that document, with a maximum of 1000 bytes per field<br>
			3) score - the score for the search result, from 0 to 1<br>
			4) fragment - the fragment where the search string was found in the data if this parameter is set to true<br>
			5) distance - the distance from the point specified is this was a spatial search<br>
			<br>
			
			In the case where there are multiple fields in the document then all the fields will be returned with the result. This 
			makes it easy to display the data and results and doesn't have a very large impact on the performance writing the data 
			over the wire.<br><br>
			
			The results are a Java list of maps and as such can be de-serialized into an object using the standard XML serialization class 
			XMLDecoder, alternatively there is a utility in Ikube to do just that, SerializationUtilities#deserialize(String xml). For other platforms 
			they would then parse the XML or de-serialize as necessary to render the results.<br><br>
			
			<a href="<c:url value="/docs/results.xml"/>" target="_top">Here</a> is an example of some results in XML format. This is a result 
			from a search against the GeoSpatial index, using the following properties:<br><br>
			
			* index name - geospatial<br>
			* search string - cape AND town<br>
			* search field - name<br>
			* fragment - true<br>
			* first result - 0<br>
			* max results - 10<br>
			* latitude - -33.9693580<br>
			* longitude - 18.4622110<br><br>
			
			The rest web service is published by Jersey, and protected by Spring Security. To access this web service you need to be logged in with one of the userids 
			and passwords defined in the spring-security.xml which is in the configuration folder.<br><br>
			
			The final map in the list will have two fields, the total results that were returned by the search and the duration for the search, just 
			for interests' sake. The last map will also contain the spelling corrections, or the original search string if there were no spelling errors.
			<br><br>
			
			<strong>Examples of the rest API and parameters for the web service are:</strong>
			<br><br>
			
			<h3>Single search : </h3><textarea rows="3" cols="80"><c:out value="${single}" /></textarea><br><br>
			<h3>Single multi : </h3><textarea rows="3" cols="80"><c:out value="${multi}" /></textarea><br><br>
			<h3>Single multi all : </h3><textarea rows="3" cols="80"><c:out value="${multiAll}" /></textarea><br><br>
			<h3>Single multi sorted : </h3><textarea rows="3" cols="80"><c:out value="${multiSorted}" /></textarea><br><br>
			<h3>Single multi spatial : </h3><textarea rows="3" cols="80"><c:out value="${multiSpatial}" /></textarea><br><br>
			<h3>Single multi spatial all : </h3><textarea rows="3" cols="80"><c:out value="${multiSpatialAll}" /></textarea><br><br>
			<h3>Single complex : </h3><textarea rows="3" cols="80"><c:out value="${complex}" /></textarea><br><br>
			
			<br><br>
	
</div>