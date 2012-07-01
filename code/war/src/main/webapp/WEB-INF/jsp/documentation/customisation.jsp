<%@ page errorPage="/WEB-INF/jsp/error.jsp" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content">
			<span class="top-content-header">searching</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>access</strong>&nbsp;
			The access to search results is a via a rest web service. Method signatures for the web service methods for 
			searching are:<br><br>
			
			1) Search in a single field in the index - 
				<a href="http://ikube.dyndns.org:8080/ikube/service/search/single?indexName=geospatial&searchStrings=cape%20AND%20town%20AND%20university&searchFields=name&fragment=true&firstResult=0&maxResults=10">go</a><br>
			2) Search multiple fields in the index - 
				<a href="http://ikube.dyndns.org:8080/ikube/service/search/multi?indexName=geospatial&searchStrings=cape+AND+town+AND+university%3Bsouth+africa&searchFields=name%3Bcountry&fragment=true&firstResult=0&maxResults=10">go</a><br>
			3) Search all the fields in the index - 
				<a href="http://ikube.dyndns.org:8080/ikube/service/search/multi/all?indexName=geospatial&searchStrings=cape+AND+town+AND+university%3Bsouth+africa&fragment=true&firstResult=0&maxResults=10">go</a><br>
			4) Search all the fields in the index and sort them according to the sort fields - 
				<a href="http://ikube.dyndns.org:8080/ikube/service/search/multi/sorted?indexName=geospatial&searchStrings=cape+town+university%3Bsouth+africa&searchFields=name%3Bcountry&sortFields=name%3Bcountry&fragment=true&firstResult=0&maxResults=10">go</a><br>
			5) Search a single field and sort the results according to distance from a point - 
				<a href="http://ikube.dyndns.org:8080/ikube/service/search/multi/spatial?indexName=geospatial&searchStrings=cape%20town%20university&searchFields=name&fragment=true&firstResult=0&maxResults=10&distance=10&latitude=-33.95796&longitude=18.46082">go</a><br>
			6) Search all the fields and sort the results according to distance from a point - 
				<a href="http://ikube.dyndns.org:8080/ikube/service/search/multi/spatial?indexName=geospatial&searchStrings=cape%20town%20university&fragment=true&firstResult=0&maxResults=10&distance=10&latitude=-33.95796&longitude=18.46082">go</a><br>
			
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
				results around. This will also be used to sort the results, radiating outward from this point.<br><br>
				
			In all cases the result is a string. This string is a serialized list of maps. Each map is one result. A result consists of four fields:<br><br>
			
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
			
		</td>
	</tr>
	<tr>
		<td class="td-content">
			<strong>API</strong>&nbsp;
			The API and parameters for the web service are:<br><br>
			
			Search a single field in a single index: http://localhost:8080/ikube/service/search/single<br>
			Parameters:<br>
			1) indexName=geospatial<br>
			2) searchStrings=cape+AND+town+AND+university<br>
			3) searchFields=name<br>
			4) fragment=true<br>
			5) firstResult=0<br>
			6) maxResults=10<br><br>
			
			Search multiple fields with multiple search strings in an index: http://localhost:8080/ikube/service/search/multi<br>
			Parameters:<br>
			1) indexName=geospatial<br>
			2) searchStrings=cape+AND+town+AND+university%3Bsouth+africa<br>
			3) searchFields=name%3Bcountry<br>
			4) fragment=true<br>
			5) firstResult=0<br>
			6) maxResults=10<br><br>
			
			Search all the fields in an index with one or more search strings: http://localhost:8080/ikube/service/search/multi/all<br>
			Parameters:<br>
			1) indexName=geospatial<br>
			2) searchStrings=cape+AND+town+AND+university%3Bsouth+africa<br>
			3) fragment=true<br>
			4) firstResult=0<br>
			5) maxResults=10<br><br>
			
			Search all the fields in the index with one or more search strings and sort the results according to one or more fields: http://localhost:8080/ikube/service/search/multi/sorted<br>
			Parameters:<br>
			1) indexName=geospatial<br>
			2) searchStrings=cape+AND+town+AND+university%3Bsouth+africa<br>
			3) searchFields=name%3Bcountry<br>
			4) sortFields=name%3Bcountry<br>
			5) fragment=true<br>
			6) firstResult=0<br>
			7) maxResults=10<br><br>
			
			Search multiple fields in a spatial index, with one or more search strings: http://localhost:8080/ikube/service/search/multi/spatial<br>
			Parameters:<br>
			1) indexName=geospatial<br>
			2) searchStrings=cape+AND+town+AND+university%3Bsouth+africa<br>
			3) searchFields=name%3Bcountry<br>
			4) fragment=true<br>
			5) firstResult=0<br>
			6) maxResults=10<br>
			7) distance=50<br>
			8) latitude=18.46082<br>
			9) longitude=-33.95796<br><br>
			
			Search all the fields in a spatial index, with one or more search strings: http://localhost:8080/ikube/service/search/multi/spatial/all<br>
			Parameters:<br>
			1) indexName=geospatial<br>
			2) searchStrings=cape+town+university<br>
			3) fragment=true<br>
			4) firstResult=0<br>
			5) maxResults=10<br>
			6) distance=10<br>
			7) latitude=18.46082<br>
			8) longitude=-33.95796<br><br>
			
		</td>
	</tr>
	
	<tr>
		<td class="td-content">
			<strong>more questions</strong>&nbsp;
			If you have any questions please feel free to give me a shout at michael dot couck at gmail dot com.<br><br>
			Happy searching!
		</td>
	</tr>
</table>