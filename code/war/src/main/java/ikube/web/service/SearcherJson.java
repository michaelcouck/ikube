package ikube.web.service;

import ikube.IConstants;
import ikube.model.Search;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Path looks like this: http://localhost:9080/ikube/service/search/json/multi
 * 
 * @author Michael couck
 * @since 21.01.12
 * @version 01.00
 */
@Component
@Path(SearcherJson.SEARCH)
@Scope(Resource.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class SearcherJson extends Searcher {

	public static final String SEARCH = "/search/json";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherJson.class);

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.SINGLE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchSingle(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment, @QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		ArrayList<HashMap<String, String>> results = searcherService.searchSingle(indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMulti(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment, @QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStringsArray, searchFieldsArray, fragment, firstResult,
				maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_SORTED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMultiSorted(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.SORT_FIELDS) final String sortFields, @QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult, @QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		String[] sortFieldsArray = StringUtils.split(sortFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSorted(indexName, searchStringsArray, searchFieldsArray, sortFieldsArray,
				fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_ALL)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMultiAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult, @QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, searchStringsArray, fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_SPATIAL)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMultiSpacial(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment, @QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacial(indexName, searchStringsArray, searchFieldsArray, fragment,
				firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_SPATIAL_ALL)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMultiSpacialAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult, @QueryParam(value = IConstants.MAX_RESULTS) final int maxResults,
			@QueryParam(value = IConstants.DISTANCE) final int distance, @QueryParam(value = IConstants.LATITUDE) final String latitude,
			@QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStringsArray, fragment, firstResult, maxResults,
				distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_ADVANCED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMultiAdvanced(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchField,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment, @QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchField, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAdvanced(indexName, searchStringsArray, searchFieldsArray, fragment,
				firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.NUMERIC_ALL)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchNumericAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult, @QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchNumericAll(indexName, searchStringsArray, fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.NUMERIC_RANGE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchNumericRange(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult, @QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchNumericRange(indexName, searchStringsArray, fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.COMPLEX)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchComplex(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.TYPE_FIELDS) final String typeFields, @QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult, @QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		String[] typeFieldsArray = StringUtils.split(typeFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchComplex(indexName, searchStringsArray, searchFieldsArray, typeFieldsArray, fragment,
				firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.COMPLEX_SORTED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchComplexSorted(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings, @QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.TYPE_FIELDS) final String typeFields, @QueryParam(value = IConstants.SORT_FIELDS) final String sortFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment, @QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		String[] typeFieldsArray = StringUtils.split(typeFields, SEPARATOR);
		String[] sortFieldsArray = StringUtils.split(sortFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchComplexSorted(indexName, searchStringsArray, searchFieldsArray, typeFieldsArray,
				sortFieldsArray, fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@POST
	@Override
	@Path(SearcherJson.COMPLEX_SORTED_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchComplexSorted(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		if (search != null && search.getSearchStrings() != null && search.getSearchStrings().size() > 0) {
			searcherService.searchComplexSorted(search);
		}
		if (search.getIndexName() == null) {
			search.setIndexName("");
		}
		if (search.getSearchFields() == null) {
			search.setSearchFields(new ArrayList<String>());
		}
		if (search.getSearchStrings() == null) {
			search.setSearchStrings(new ArrayList<String>());
		}
		if (search.getTypeFields() == null) {
			search.setTypeFields(new ArrayList<String>());
		}
		if (search.getSortFields() == null) {
			search.setSortFields(new ArrayList<String>());
		}
		return buildResponse(search);
	}

}