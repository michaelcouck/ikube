package ikube.search;

import ikube.model.Search;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the 'service' layer for searching. This interface has several methods for searching, specifically
 * with multiple parameters, including the name of the index, the search strings and fields, and so on. It also has
 * a more convenience method(s) with a {@link ikube.model.Search} object, which decomposes to reveal all the required
 * parameters for a complex search, including the above, i.e. index name and so on.
 *
 * Typically the {@link ikube.model.Search} object methods will be used from a Json call, and the other methods
 * will be called by systems that define parameters in the url for convenience.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Service
public interface ISearcherService {

    /**
     * Basic search, specifying some fields to search in the index and their values.
     *
     * @param indexName     the name of the index to search
     * @param searchStrings the search strings to search for in the index
     * @param searchFields  the fields that will be matched against the search strings
     * @param fragment      whether to add the fragment of text to the search results
     * @param firstResult   the first result in the index, for paging
     * @param maxResults    the maximum results to return
     * @return the list of results from the search
     */
    ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults);

    /**
     * Similar to the above with the addition of sort fields. Multiple fields can be used to sort on.
     *
     * @param indexName     the name of the index to search
     * @param searchStrings the search strings to search for in the index
     * @param searchFields  the fields that will be matched against the search strings
     * @param sortFields    the fields in the index to sort on
     * @param fragment      whether to add the fragment of text to the search results
     * @param firstResult   the first result in the index, for paging
     * @param maxResults    the maximum results to return
     * @return the list of results from the search
     */
    ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final String[] sortFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults);

    /**
     * Similar to the above except that the fields can be type specified, for ranges and so on.
     *
     * @param indexName     the name of the index to search
     * @param searchStrings the search strings to search for in the index
     * @param searchFields  the fields that will be matched against the search strings
     * @param typeFields    the types of fields specified, like number or range for example
     * @param sortFields    the fields in the index to sort on
     * @param fragment      whether to add the fragment of text to the search results
     * @param firstResult   the first result in the index, for paging
     * @param maxResults    the maximum results to return
     * @return the list of results from the search
     */
    ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final String[] typeFields,
            final String[] sortFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults);

    /**
     * Similar to the above except that the results are based on a geospatial field, that the results are within
     * a certain distance from the origin specified by the user, and the results will be returned sorted by distance
     * from the origin.
     *
     * @param indexName     the name of the index to search
     * @param searchStrings the search strings to search for in the index
     * @param searchFields  the fields that will be matched against the search strings
     * @param typeFields    the types of fields specified, like number or range for example
     * @param fragment      whether to add the fragment of text to the search results
     * @param firstResult   the first result in the index, for paging
     * @param maxResults    the maximum results to return
     * @param distance      the maximum distance to the origin to return results for
     * @param latitude      the longitude of the origin
     * @param longitude     the latitude of the origin
     * @return the list of results from the search
     */
    ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final String[] typeFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults,
            final int distance,
            final double latitude,
            final double longitude);

    /**
     * This search will accomplish all the above but using the Json provided object as a convenience for the gui.
     *
     * @param search the search object to use for the search, populated with all the goodies that are necessary to execute the search
     * @return the list of results from the search
     */
    Search search(
            final Search search);

    /**
     * This is a convenience method that will search every single field in every single index, i.e. very expensive, only for
     * convenience, generally should not be exposed to the user, this is an administration function.
     *
     * @param search the search object to use for the search, populated with all the goodies that are necessary to execute the search
     * @return the list of results from the search
     */
    Search searchAll(
            final Search search);

    /**
     * This method, as the one above ({@link ikube.search.ISearcherService#search(ikube.model.Search)}
     * returns a search but this method is exclusively local.
     *
     * @param search the search to perform
     * @return the search object, populated with results probably
     */
    Search doSearch(
            final Search search);

}