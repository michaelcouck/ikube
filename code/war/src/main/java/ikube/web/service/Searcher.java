package ikube.web.service;

import javax.ws.rs.core.Response;

/**
 * This is the base class for all searcher web services, common logic and properties. Also all the methods that are exposed to clients for
 * xml and Json responses are defined here. This class could be seen as the API that is exposed to iKube clients.
 * 
 * @author Michael couck
 * @since 20.11.12
 * @version 01.00
 */
public abstract class Searcher extends Resource {

	/**
	 * Does a search on a single field on the index defined in the parameter list.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchString the search string to search for
	 * @param searchField the search field in the index
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return a serialized string of the results from the search
	 */
	abstract Response searchSingle(final String indexName, final String searchStrings, final String searchFields, final boolean fragment,
			final int firstResult, final int maxResults);

	/**
	 * Does a search on multiple fields and multiple search strings.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the search fields in the index
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return a serialized string of the results from the search
	 */
	abstract Response searchMulti(final String indexName, final String searchStrings, final String searchFields, final boolean fragment,
			final int firstResult, final int maxResults);

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
	 * @return a serialized string of the results from the search
	 */
	abstract Response searchMultiSorted(final String indexName, final String searchStrings, final String searchFields,
			final String sortFields, final boolean fragment, final int firstResult, final int maxResults);

	/**
	 * This is a convenient method to search for the specified strings in all the fields.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param fragment whether to generate a fragment from the stored data for the matches
	 * @param firstResult the first result for paging
	 * @param maxResults the maximum results for paging
	 * @return the results from the search serialized to an xml string
	 */
	abstract Response searchMultiAll(final String indexName, final String searchStrings, final boolean fragment, final int firstResult,
			final int maxResults);

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
	 * @return the results from the search serialized to an xml string
	 */
	abstract Response searchMultiSpacial(final String indexName, final String searchStrings, final String searchFields,
			final boolean fragment, final int firstResult, final int maxResults, final int distance, final String latitude,
			final String longitude);

	/**
	 * This method will search all the fields in the spatial index, and sort the results by distance from a point.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings, note that all the search strings will be used to search all the fields
	 * @param fragment whether the results should contain the fragment
	 * @param firstResult the first result to page
	 * @param maxResults the max results to return, for paging
	 * @param distance the distance around the point specified to return results for
	 * @param latitude the latitude for the starting point for sorting the results from, and for the distance calculation
	 * @param longitude the longitude for the starting point for sorting the results from, and for the distance calculation
	 * @return the results around the point specified, going the maximum distance specified, sorted according to the distance from teh point
	 *         specified
	 */
	abstract Response searchMultiSpacialAll(final String indexName, final String searchStrings, final boolean fragment,
			final int firstResult, final int maxResults, final int distance, final String latitude, final String longitude);

	/**
	 * TODO Comment me.
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param searchField
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	abstract Response searchMultiAdvanced(final String indexName, final String searchStrings, final String searchField,
			final boolean fragment, final int firstResult, final int maxResults);

	/**
	 * TODO Comment me.
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	abstract Response searchNumericAll(final String indexName, final String searchStrings, final boolean fragment, final int firstResult,
			final int maxResults);

	/**
	 * TODO Comment me.
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	abstract Response searchNumericRange(final String indexName, final String searchStrings, final boolean fragment, final int firstResult,
			final int maxResults);

	/**
	 * TODO Comment me.
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param searchFields
	 * @param typeFields
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	abstract Response searchComplex(final String indexName, final String searchStrings, final String searchFields, final String typeFields,
			final boolean fragment, final int firstResult, final int maxResults);
}