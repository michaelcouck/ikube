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
			The access to search results is a via a web service. Method signatures for the web service methods for 
			searching are:<br><br>
			
			1) Search in a single field in the index - ISearcherWebService#searchSingle(String indexName, String searchString, 
				String searchField, boolean fragment, int firstResult, int maxResults)<br>
			2) Search multiple fields in the index - ISearcherWebService#searchMulti(String indexName, String[] searchStrings, 
				String[] searchFields, boolean fragment, int firstResult, int maxResults)<br>
			3) Search multiple fields in the index and sort them according to particular fields - ISearcherWebService#searchMultiSorted(String 
				indexName, String[] searchStrings, String[] searchFields, String[] sortFields, boolean fragment, int firstResult, int maxResults)<br>
			4) Search all the fields in the index and sort them according to particular fields - ISearcherWebService#searchMultiAll(String 
				indexName, String[] searchStrings, boolean fragment, int firstResult, int maxResults)<br>
			5) Search multiple fields and sort the results in ascending order by distance from a point(latitude and longitude) - 
				ISearcherWebService#searchMulti(String indexName, String[] searchStrings, String[] searchFields, boolean fragment, 
				int firstResult, int maxResults, int distance, double latitude, double longitude)<br><br>
			
			Parameters for searching:<br>
			1) Index name - The index name is the name of the index that you want to search.<br> 
			2) Search string(s) - The search string is the search string(s), which could have Lucene syntax, for example for wild card searches 
				and the like. Please refer to the <a href="http://lucene.apache.org/java/3_0_0/queryparsersyntax.html">Lucene syntax</a> 
				documentation for all the possibilities that you can use for searching indexes. Keep in mind that a wild card search will iterate through all 
				the documents fields' running a Levenshtein distance on the data, and will be very expensive.<br><br>
			3) Search field(s) - The search fields are the fields in the Lucene index that you want to search.<br>
			4) Sort field(s) - The sort fields are the fields that will be sorted on, in the order that they are specified.<br>
			5) Fragment - Whether to create a fragment from the data stored in the index. Note that fragments can only be generated 
				if the 'stored' parameter is specified in the Lucene configuration, i.e. that the data is in fact stored in the index as is.<br>
			6) First result - This is the first result as it appears in the list of results. This can be specified to allow paging, along with the 
				max results parameter.<br>
			7) Max results - This parameter will limit the results to a set, perhaps 10 or even 100, starting at the point in the results where 
				the first result parameter was specified.<br>
			8) Distance - The distance is for spatial searches. This defines the best fit distance that should be taken into consideration when 
				allocating the results from a point. The value of this should be around 10.
			9) Latitude and longitude - These are the points that will be used as the origin of the search, i.e. the starting point to find 
				results around. This will also be used to sort the results, radiating outward from this point.<br><br>
				
			For other platforms a client will need to be generated using tools for that platform from the WSDL, which is published on a particular 
			port during the startup of Ikube.<br><br>
			
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
			
			The web service will automatically be published to http://169.254.107.201:8081/ikube/service/ISearcherWebService?wsdl, replacing 
			the ip with the ip of the machine, using the standard Java web service publisher, using Endpoint endpoint = Endpoint.publish(url, 
			implementor). This will bind the web service to the defined url and port. If there are more than one servers running on the same 
			machine then the port will be incremented before publishing, starting at 8081. So for three servers on one machine the ports would be 
			8081, 8082, 8083.<br><br>
			
			It is possible to use the classes included in Ikube to do the search, this does however mean putting ikube code on the classpath. There 
			is an example how to do this in the ikube.action.Searcher class. Here is the required code:<br><br>
			
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);<br>
			String xml = searchRemote.searchSingle(indexName, searchString, fieldName, fragment, start, end);<br>
			List&lt;Map&lt;String, String&gt;&gt; results = (List&lt;Map&lt;String, String&gt;&gt;) SerializationUtilities.deserialize(xml);<br><br>
			
			The final map in the list will have two fields, the total results that were returned by the search and the duration for the search, just 
			for interests' sake. The last map will also contain the spelling corrections, or the original search string if there were no spelling errors.
			<br><br>
			
			For convenience there are also tags for iterating over the results. These can be used as is. You just need to extract the jar 
			from the war and look at the include.jsp for an example of how to use the tags. As well as this there is a spelling checker 
			tag that can be added. This tag has an example of the usage in the menu.jsp in the war. At the time of writing there 
			was only spelling checking for English but to add more languages all you would need to do is to add text files with word lists 
			from other languages to the ./spellingIndex directory, something like italian.txt for example.<br><br>
			
			If you have any questions please feel free to give me a shout at michael dot couck at gmail dot com.<br><br>
			
			Happy searching!
		</td>
	</tr>
</table>