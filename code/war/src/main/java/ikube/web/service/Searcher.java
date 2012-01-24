package ikube.web.service;

import ikube.service.ISearcherWebService;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Path looks like this: http://localhost:9080/ikube/service/search/multi
 * 
 * Trial and error:
 * 
 * <pre>
 * Doesn't work.
 * GenericEntity<ArrayList<HashMap<String, String>>> entity = new GenericEntity<ArrayList<HashMap<String, String>>>(results) {
 * 		// Abstract implementation
 * };
 * Doesn't work.
 * return Response.ok().entity(results.toArray(new HashMap[results.size()])).build();
 * Could be a lot of work.
 * MessageBodyWriter<ArrayList<HashMap<String, String>>> messageBodyWriter = null;
 * </pre>
 * 
 * @author Michael couck
 * @since 21.01.12
 * @version 01.00
 */
@Component
@Path("/search")
@Scope("request")
@Produces(MediaType.TEXT_PLAIN)
public class Searcher {

	@Autowired
	private ISearcherWebService searcherWebService;

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
	@GET
	@Path("/single")
	@Consumes(MediaType.TEXT_PLAIN)
	public String searchSingle(@QueryParam(value = "indexName") final String indexName,
			@QueryParam(value = "searchString") final String searchString, @QueryParam(value = "searchField") final String searchField,
			@QueryParam(value = "fragment") final boolean fragment, @QueryParam(value = "firstResult") final int firstResult,
			@QueryParam(value = "maxResults") final int maxResults) {
		ArrayList<HashMap<String, String>> results = searcherWebService.searchSingle(indexName, searchString, searchField, fragment,
				firstResult, maxResults);
		return SerializationUtilities.serialize(results);
	}

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
	@GET
	@Path("/multi")
	@SuppressWarnings("unused")
	@Consumes(MediaType.TEXT_PLAIN)
	String searchMulti(@QueryParam(value = "indexName") final String indexName,
			@QueryParam(value = "searchStrings") final String[] searchStrings,
			@QueryParam(value = "searchFields") final String[] searchFields, @QueryParam(value = "fragment") final boolean fragment,
			@QueryParam(value = "firstResult") final int firstResult, @QueryParam(value = "maxResults") final int maxResults) {
		return null;
	}

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
	@GET
	@Path("/multi/sorted")
	@SuppressWarnings("unused")
	@Consumes(MediaType.TEXT_PLAIN)
	String searchMultiSorted(@QueryParam(value = "indexName") final String indexName,
			@QueryParam(value = "searchStrings") final String[] searchStrings,
			@QueryParam(value = "searchFields") final String[] searchFields, @QueryParam(value = "sortFields") final String[] sortFields,
			@QueryParam(value = "fragment") final boolean fragment, @QueryParam(value = "firstResult") final int firstResult,
			@QueryParam(value = "maxResults") final int maxResults) {
		return null;
	}

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
	@GET
	@Path("/multi/all")
	@SuppressWarnings("unused")
	@Consumes(MediaType.TEXT_PLAIN)
	String searchMultiAll(@QueryParam(value = "indexName") final String indexName,
			@QueryParam(value = "searchStrings") final String[] searchStrings, @QueryParam(value = "fragment") final boolean fragment,
			@QueryParam(value = "firstResult") final int firstResult, @QueryParam(value = "maxResults") final int maxResults) {
		return null;
	}

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
	@GET
	@Path("/multi/spatial")
	@SuppressWarnings("unused")
	@Consumes(MediaType.TEXT_PLAIN)
	String searchMultiSpacial(@QueryParam(value = "indexName") final String indexName,
			@QueryParam(value = "searchStrings") final String[] searchStrings,
			@QueryParam(value = "searchFields") final String[] searchFields, @QueryParam(value = "fragment") final boolean fragment,
			@QueryParam(value = "firstResult") final int firstResult, @QueryParam(value = "maxResults") final int maxResults,
			@QueryParam(value = "distance") final int distance, @QueryParam(value = "latitude") final double latitude,
			@QueryParam(value = "longitude") final double longitude) {
		return null;
	}

	/**
	 * TODO Document me!
	 * 
	 * @param indexName
	 * @param searchStrings
	 * @param fragment
	 * @param firstResult
	 * @param maxResults
	 * @param distance
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	@GET
	@Path("/multi/spatial/all")
	@SuppressWarnings("unused")
	@Consumes(MediaType.TEXT_PLAIN)
	String searchMultiSpacialAll(@QueryParam(value = "indexName") final String indexName,
			@QueryParam(value = "searchStrings") final String[] searchStrings, @QueryParam(value = "fragment") final boolean fragment,
			@QueryParam(value = "firstResult") final int firstResult, @QueryParam(value = "maxResults") final int maxResults,
			@QueryParam(value = "distance") final int distance, @QueryParam(value = "latitude") final double latitude,
			@QueryParam(value = "longitude") final double longitude) {
		return null;
	}

}