package ikube.service;

import ikube.IConstants;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Path(SearcherXml.SEARCH)
@Scope(SearcherXml.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class SearcherXml {

	/** Constants for the paths to the web services. */
	public static final String REQUEST = "request";
	public static final String SERVICE = "/service";
	public static final String SEARCH = "/search";
	public static final String SINGLE = "/single";
	public static final String MULTI = "/multi";
	public static final String MULTI_SORTED = "/multi/sorted";
	public static final String MULTI_ALL = "/multi/all";
	public static final String MULTI_SPATIAL = "/multi/spatial";
	public static final String MULTI_SPATIAL_ALL = "/multi/spatial/all";
	public static final String MULTI_ADVANCED_ALL = "/multi/advanced/all";
	public static final String MULTI_SPATIAL_ALL_TABLE = "/multi/spatial/all/table";

	public static final String RESULTS_TO_TABLE = "/table";

	public static final String SEPARATOR = ",;:|";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherXml.class);

	@Autowired
	private ISearcherService searcherService;

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
	@Path(SearcherXml.SINGLE)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchSingle(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		ArrayList<HashMap<String, String>> results = searcherService.searchSingle(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

	private ResponseBuilder buildResponse() {
		return Response.status(200).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
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
	@Path(SearcherXml.MULTI)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMulti(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
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
	@Path(SearcherXml.MULTI_SORTED)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiSorted(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.SORT_FIELDS) final String sortFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		String[] sortFieldsArray = StringUtils.split(sortFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSorted(indexName, searchStringsArray, searchFieldsArray,
				sortFieldsArray, fragment, firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
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
	@Path(SearcherXml.MULTI_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, searchStringsArray, fragment, firstResult,
				maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
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
	@Path(SearcherXml.MULTI_SPATIAL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiSpacial(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacial(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

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
	@GET
	@Path(SearcherXml.MULTI_SPATIAL_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiSpacialAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStringsArray, fragment,
				firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

	@GET
	@Path(SearcherXml.MULTI_ADVANCED_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiAdvanced(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchField,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFields = { searchField };
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAdvanced(indexName, searchStringsArray, searchFields,
				fragment, firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

}