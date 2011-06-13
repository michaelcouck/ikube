package ikube.service;

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
	 * @return a serialized string of the results from the search
	 */
	@WebMethod
	@WebResult(name = "result")
	String searchSingle(@WebParam(name = "indexName") final String indexName, @WebParam(name = "searchString") final String searchString,
			@WebParam(name = "searchField") final String searchField, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

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
	 * @return a serialized string of the results from the search
	 */
	@WebMethod
	@WebResult(name = "result")
	String searchMulti(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults);

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
	 * @return a serialized string of the results from the search
	 */
	@WebMethod
	@WebResult(name = "result")
	String searchMultiSorted(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "sortFields") final String[] sortFields, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

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
	@WebMethod
	@WebResult(name = "result")
	String searchMultiAll(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "fragment") final boolean fragment,
			@WebParam(name = "firstResult") final int firstResult, @WebParam(name = "maxResults") final int maxResults);

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
	@WebMethod
	@WebResult(name = "result")
	String searchSpacialMulti(@WebParam(name = "indexName") final String indexName,
			@WebParam(name = "searchStrings") final String[] searchStrings, @WebParam(name = "searchFields") final String[] searchFields,
			@WebParam(name = "fragment") final boolean fragment, @WebParam(name = "firstResult") final int firstResult,
			@WebParam(name = "maxResults") final int maxResults, @WebParam(name = "distance") final int distance,
			@WebParam(name = "latitude") final double latitude, @WebParam(name = "longitude") final double longitude);

}