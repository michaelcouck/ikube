package ikube.web.service;

import static ikube.IConstants.DISTANCE;
import static ikube.IConstants.FIRST_RESULT;
import static ikube.IConstants.FRAGMENT;
import static ikube.IConstants.INDEX_NAME;
import static ikube.IConstants.LATITUDE;
import static ikube.IConstants.LONGITUDE;
import static ikube.IConstants.MAX_RESULTS;
import static ikube.IConstants.SEARCH_FIELDS;
import static ikube.IConstants.SEARCH_STRINGS;
import static ikube.IConstants.SORT_FIELDS;
import static ikube.IConstants.TYPE_FIELDS;
import ikube.model.Search;

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
@Scope(Resource.REQUEST)
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
@Path(SearcherJson.SEARCH + SearcherJson.JSON)
public class SearcherJson extends Searcher {

	public static final String JSON = "/json";

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.SIMPLE)
	public Response search(//
			@QueryParam(value = INDEX_NAME) final String indexName, //
			@QueryParam(value = SEARCH_STRINGS) final String searchStrings, //
			@QueryParam(value = SEARCH_FIELDS) final String searchFields, //
			@QueryParam(value = FRAGMENT) final boolean fragment, //
			@QueryParam(value = FIRST_RESULT) final int firstResult, //
			@QueryParam(value = MAX_RESULTS) final int maxResults) {
		Object results = searcherService.search(indexName, split(searchStrings), split(searchFields), fragment, firstResult, maxResults);
		return buildJsonResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.SORTED)
	public Response search(//
			@QueryParam(value = INDEX_NAME) final String indexName, //
			@QueryParam(value = SEARCH_STRINGS) final String searchStrings, //
			@QueryParam(value = SEARCH_FIELDS) final String searchFields, //
			@QueryParam(value = SORT_FIELDS) final String sortFields, //
			@QueryParam(value = FRAGMENT) final boolean fragment, //
			@QueryParam(value = FIRST_RESULT) final int firstResult, //
			@QueryParam(value = MAX_RESULTS) final int maxResults) {
		Object results = searcherService.search(indexName, split(searchStrings), split(searchFields), split(sortFields), fragment, firstResult, maxResults);
		return buildJsonResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.SORTED_TYPED)
	public Response search(//
			@QueryParam(value = INDEX_NAME) final String indexName, //
			@QueryParam(value = SEARCH_STRINGS) final String searchStrings, //
			@QueryParam(value = SEARCH_FIELDS) final String searchFields, //
			@QueryParam(value = TYPE_FIELDS) final String typeFields, //
			@QueryParam(value = SORT_FIELDS) final String sortFields, //
			@QueryParam(value = FRAGMENT) final boolean fragment, //
			@QueryParam(value = FIRST_RESULT) final int firstResult, //
			@QueryParam(value = MAX_RESULTS) final int maxResults) {
		Object results = searcherService.search(indexName, split(searchStrings), split(searchFields), split(typeFields), split(sortFields), fragment,
				firstResult, maxResults);
		return buildJsonResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.GEOSPATIAL)
	public Response search(//
			@QueryParam(value = INDEX_NAME) final String indexName, //
			@QueryParam(value = SEARCH_STRINGS) final String searchStrings, //
			@QueryParam(value = SEARCH_FIELDS) final String searchFields, //
			@QueryParam(value = TYPE_FIELDS) final String typeFields, //
			@QueryParam(value = FRAGMENT) final boolean fragment, //
			@QueryParam(value = FIRST_RESULT) final int firstResult, //
			@QueryParam(value = MAX_RESULTS) final int maxResults, //
			@QueryParam(value = DISTANCE) final int distance, //
			@QueryParam(value = LATITUDE) final double latitude, //
			@QueryParam(value = LONGITUDE) final double longitude) {
		Object results = searcherService.search(indexName, split(searchStrings), split(searchFields), split(typeFields), fragment, firstResult, maxResults,
				distance, latitude, longitude);
		return buildJsonResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@POST
	@Override
	@Consumes(MediaType.APPLICATION_JSON)
	public Response search(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		Object results = searcherService.search(search);
		return buildJsonResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@POST
	@Override
	@Path(SearcherJson.ALL)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchAll(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		Object results = searcherService.searchAll(search);
		return buildJsonResponse(results);
	}

}