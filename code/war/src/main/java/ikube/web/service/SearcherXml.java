package ikube.web.service;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SearcherXml extends Searcher {

	public static final String SEARCH = "/search";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherXml.class);

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
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

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
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
	 * {@inheritDoc}
	 */
	@GET
	@Override
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
	 * {@inheritDoc}
	 */
	@GET
	@Override
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
	 * {@inheritDoc}
	 */
	@GET
	@Override
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
	 * {@inheritDoc}
	 */
	@GET
	@Override
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

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.MULTI_ADVANCED)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiAdvanced(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchField,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchField, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAdvanced(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.NUMERIC_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchNumericAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchNumericAll(indexName, searchStringsArray, fragment, firstResult,
				maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.NUMERIC_RANGE)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchNumericRange(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchNumericRange(indexName, searchStringsArray, fragment,
				firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.COMPLEX)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchComplex(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.TYPE_FIELDS) final String typeFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		String[] typeFieldsArray = StringUtils.split(typeFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchComplex(indexName, searchStringsArray, searchFieldsArray,
				typeFieldsArray, fragment, firstResult, maxResults);
		return buildResponse().entity(SerializationUtilities.serialize(results)).build();
	}

}