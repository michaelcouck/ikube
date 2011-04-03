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
			<strong>access</strong>&nbsp;<br>
			The access to search results is a via a web service and a servlet. Method signatures for the web service methods for 
			searching are:<br><br>
			
			1) Search in a single field in the index - ISearcherWebService#searchSingle(String indexName, String searchString, 
				String searchField, boolean fragment, int start, int end)<br>
			2) Search multiple fields in the index - ISearcherWebService#searchMulti(String indexName, String[] searchStrings, 
				String[] searchFields, boolean fragment, int start, int end)<br>
			3) Search multiple fields in the index and sort them according to particular fields - ISearcherWebService#searchMultiSorted(String 
				indexName, String[] searchStrings, String[] searchFields, String[] sortFields, boolean fragment, int start, int end)<br><br>
			
			The index name is of course the name of the index that you want to search. The search string is the search string, which could have 
			Lucene syntax, for example for wild card searches and the like. Please refer to the 
			<a href="http://lucene.apache.org/java/3_0_0/queryparsersyntax.html">Lucene syntax</a> 
			for queries for all the possibilities that 	you can use for searching indexes. Keep in mind that a wild card search will iterate through all 
			the documents fields' running a Levenshtein distance on the data, and will be very expensive.<br><br>  
			
			In all cases the result is a string. This string is a serialized list of maps. Each map is one result. A result consists of four fields:<br><br>
			
			1) The offset in the Lucene index of the Lucene document<br>
			2) The fields and their content in that document, with a maximum of 1000 bytes per field<br>
			3) The score for the search result<br>
			4) The fragment where the search string was found in the data if this parameter is set to true<br><br>
			
			In the case where there are multiple fields in the document then all the fields will be returned with the result. This 
			makes it easy to display the data and results and doesn't have a very large impact on the performance writing the data 
			over the wire.<br><br>
			
			The results are a Java list of maps and as such can be de-serialized into an object using the standard XML serialization class 
			XMLDecoder, alternatively there is a utility in Ikube to do just that, SerializationUtilities#deserialize(String xml). For other platforms 
			they would then parse the XML or de-serialize as necessary to render the results.<br><br>
			
			<a href="<c:url value="/docs/results.xml"/>" target="_top">Here</a> is an example of some results in XML format. There is one result
			 and one field in the Lucene index for the document called 'content'.<br><br>
			
			The web service will automatically be published to http://169.254.107.201:8081/ikube/service/ISearcherWebService?wsdl, replacing 
			the ip with the ip of the machine, using the standard Java web service publisher, using Endpoint endpoint = Endpoint.publish(url, 
			implementor). This will bind the web service to the defined url and port. If there are more than one servers running on the same 
			machine then the port will be incremented before publishing, starting at 8081. So for three servers on one machine the ports would be 
			8081, 8082, 8083.<br><br>
			
			It is possible to use the classes included in Ikube to do the search, this does however mean putting ikube code on the classpath. There 
			is an example how to do this in the SearcherWebServiceExecuter. Here is the required code:<br><br>
			
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);<br>
			String xml = searchRemote.searchSingle(indexName, searchString, fieldName, fragment, start, end);<br>
			List&lt;Map&lt;String, String&gt;&gt; results = (List&lt;Map&lt;String, String&gt;&gt;) SerializationUtilities.deserialize(xml);<br><br>
			
			The final map in the list will have two fields, the total results that were returned by the search and the duration for the search, just 
			for interests' sake.<br><br>
			
			For convenience there are also tags for iterating over the results. These can be used as is. You just need to extract the jar 
			from the war and look at the results.jspf for an example of how to use the tags. As well as this there is a spelling checker 
			tag that can be added. This tag has an example of the usage in the menu.jsp in the war. At the time of writing there 
			was only spelling checking for Englich but to add more languages all you would need to do is to add the words from other 
			languages to the words.txt file in the war and Ikube will index the language words.<br><br>
			
			If you have any questions please feel free to give me a shout at michael dot couck at gmail dot com.<br><br>
			
			Happy searching!
		</td>
	</tr>
</table>