package ikube.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Web service access to the index search results. This would then be the public API entry point.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = ISearcherWebService.NAME, targetNamespace = ISearcherWebService.NAMESPACE, serviceName = ISearcherWebService.SERVICE)
public interface ISearcherWebService {

	String NAME = "searcher";
	String SERVICE = "searcher";
	String NAMESPACE = "http://ikube.search/";

	String PUBLISHED_PATH = "/" + ISearcherWebService.class.getName().replace(".", "/") + "?wsdl";

	/**
	 * Does a search on a single field on the index defined in the parameter list.
	 * 
	 * @param indexName
	 *            the name of the index to search
	 * @param searchString
	 *            the search string to search for
	 * @param searchField
	 *            the search field in the index
	 * @param fragment
	 *            whether to add the text fragments to the results
	 * @param firstResult
	 *            the start document in the index, for paging
	 * @param maxResults
	 *            the end document in the index, also for paging
	 * @return a serialised string of the results from the search
	 */
	String searchSingle(String indexName, String searchString, String searchField, boolean fragment, int firstResult, int maxResults);

	/**
	 * Does a search on multiple fields and multiple search strings.
	 * 
	 * @param indexName
	 *            the name of the index to search
	 * @param searchStrings
	 *            the search strings to search for
	 * @param searchFields
	 *            the search fields in the index
	 * @param fragment
	 *            whether to add the text fragments to the results
	 * @param firstResult
	 *            the start document in the index, for paging
	 * @param maxResults
	 *            the end document in the index, also for paging
	 * @return a serialised string of the results from the search
	 */
	String searchMulti(String indexName, String[] searchStrings, String[] searchFields, boolean fragment, int firstResult, int maxResults);

	/**
	 * Does a search on multiple fields and multiple search strings and sorts the results according the sort fields.
	 * 
	 * @param indexName
	 *            the name of the index to search
	 * @param searchStrings
	 *            the search strings to search for
	 * @param searchFields
	 *            the search fields in the index
	 * @param sortFields
	 *            the fields to sort the results on
	 * @param fragment
	 *            whether to add the text fragments to the results
	 * @param firstResult
	 *            the start document in the index, for paging
	 * @param maxResults
	 *            the end document in the index, also for paging
	 * @return a serialised string of the results from the search
	 */
	String searchMultiSorted(String indexName, String[] searchStrings, String[] searchFields, String[] sortFields, boolean fragment,
			int firstResult, int maxResults);

	/**
	 * TODO Comment me!
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	String searchMultiAll(String indexName, String[] searchStrings, boolean fragment, int firstResult, int maxResults);

	/**
	 * TODO Comment me!
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param searchFields
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @param distance
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	String searchSpacialMulti(String indexName, String[] searchStrings, String[] searchFields, boolean fragment, int firstResult,
			int maxResults, int distance, double latitude, double longitude);

}
