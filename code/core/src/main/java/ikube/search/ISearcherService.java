package ikube.search;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Web service access to the index search results. This would then be the public API entry point.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@WebService(name = ISearcherService.NAME, targetNamespace = ISearcherService.NAMESPACE, serviceName = ISearcherService.SERVICE)
public interface ISearcherService {

	String NAME = "searcher";
	String SERVICE = "searcher";
	String NAMESPACE = "http://ikube.search/";

	/**
	 * Does a search on a single field on the index defined in the parameter list.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchString the search string to search for
	 * @param searchField the search field in the index
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchSingle(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchString") final String searchString, @WebParam(name = "searchField") final String searchField,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults);

	/**
	 * Does a search on multiple fields and multiple search strings.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the search fields in the index
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchMulti(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults);

	/**
	 * Does a search on multiple fields and multiple search strings and sorts the results according the sort fields.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the search fields in the index
	 * @param sortFields the fields to sort the results on
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchMultiSorted(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "sortFields") final String[] sortFields, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

	/**
	 * This is a convenient method to search for the specified strings in all the fields.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param fragment whether to generate a fragment from the stored data for the matches
	 * @param firstResult the first result for paging
	 * @param maxResults the maximum results for paging
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchMultiAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

	/**
	 * This method will search for the specified strings in the specified fields, with the usual parameters like whether to generate a
	 * fragment and so on, but will sort the results according to the distance from the co-ordinate that was specified in the parameters
	 * list.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the fields to search through
	 * @param fragment whether to generate a fragment from the stored data for the matches
	 * @param firstResult the first result for paging
	 * @param maxResults the maximum results for paging
	 * @param distance the maximum distance that should be allowed for the results
	 * @param latitude the longitude of the co-ordinate to sort on
	 * @param longitude the latitude of the co-ordinate to sort on
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchMultiSpacial(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults, @WebParam(name = "distance") final int distance,
			@WebParam(name = "latitude") final double latitude, @WebParam(name = "longitude") final double longitude);

	/**
	 * This method will search the 'spatial' index and return a result set that is sorted by distance from the co-ordinate that was
	 * specified in the parameter list. Note that all the fields will be searched.
	 * 
	 * @param indexName the name of the index to search, must be a geo-spatial index
	 * @param searchStrings the search strings to use to search the fields
	 * @param fragment whether to produce a fragment of highlights from the content
	 * @param firstResult the first result in the top results, for paging
	 * @param maxResults the maximum results to return
	 * @param distance the maximum distance to search through the results
	 * @param latitude the latitude of the co-ordinate to use as the point of origin. This is the point that the results will be sorted from
	 * @param longitude the longitude of the co-ordinate to use as the point of origin
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchMultiSpacialAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults,
			@WebParam(name = "distance") final int distance, @WebParam(name = "latitude") final double latitude,
			@WebParam(name = "longitude") final double longitude);

	/**
	 * This method will do an advanced search, refining the results with a predicate like Lucene expression. The result of this is something
	 * like:
	 * 
	 * <pre>
	 * 		[any of these words] AND [this exact expression] AND [one or more of these] NOT [none of these]
	 * </pre>
	 * 
	 * Note that this is an expensive query on the index.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the strings that are used to generate the Lucene query
	 * @param searchFields the fields in the index to search
	 * @param fragment whether to generate a fragment of highlight in the results
	 * @param firstResult the first result in the set, used for paging the results
	 * @param maxResults the maximum number of results to return
	 * @return the results from the search, an {@link ArrayList} of {@link HashMap}s, that have the id, score, fragment (optional), all the
	 *         fields and their contents if they were stored in the index, and finally the statistics map, which contains the corrected
	 *         search strings, the duration of the search and the total number of results for the search
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchMultiAdvanced(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults);

	/**
	 * TODO Document me...
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchNumericAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

	/**
	 * TODO Document me...
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchNumericRange(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

	/**
	 * TODO Document me...
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param searchFields
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	@WebMethod
	@WebResult(name = "result")
	ArrayList<HashMap<String, String>> searchComplex(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "typeFields") final String[] typeFields, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

}