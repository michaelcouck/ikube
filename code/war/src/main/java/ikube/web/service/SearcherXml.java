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
@Scope(SearcherXml.REQUEST)
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_XML)
@Path(SearcherXml.SEARCH + SearcherXml.XML)
public class SearcherXml extends Searcher {

	public static final String XML = "/xml";

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.SIMPLE)
	public Response search(//
			@QueryParam(value = INDEX_NAME) final String indexName, //
			@QueryParam(value = SEARCH_STRINGS) final String searchStrings, //
			@QueryParam(value = SEARCH_FIELDS) final String searchFields, //
			@QueryParam(value = FRAGMENT) final boolean fragment, //
			@QueryParam(value = FIRST_RESULT) final int firstResult, //
			@QueryParam(value = MAX_RESULTS) final int maxResults) {
		Object results = searcherService.search(indexName, split(searchStrings), split(searchFields), fragment, firstResult, maxResults);
		return buildXmlResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.SORTED)
	public Response search(//
			@QueryParam(value = INDEX_NAME) final String indexName, //
			@QueryParam(value = SEARCH_STRINGS) final String searchStrings, //
			@QueryParam(value = SEARCH_FIELDS) final String searchFields, //
			@QueryParam(value = SORT_FIELDS) final String sortFields, //
			@QueryParam(value = FRAGMENT) final boolean fragment, //
			@QueryParam(value = FIRST_RESULT) final int firstResult, //
			@QueryParam(value = MAX_RESULTS) final int maxResults) {
		Object results = searcherService.search(indexName, split(searchStrings), split(searchFields), split(sortFields), fragment, firstResult, maxResults);
		return buildXmlResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.SORTED_TYPED)
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
		return buildXmlResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherXml.GEOSPATIAL)
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
		return buildXmlResponse(results);
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
		return buildXmlResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@POST
	@Override
	@Path(SearcherXml.ALL)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchAll(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {

		// monitorService.getIndexNames();
		// monitorService.getIndexFieldNames(indexName);

		Search search = unmarshall(Search.class, request);
		Object results = searcherService.search(search);
		return buildXmlResponse(results);
	}

}