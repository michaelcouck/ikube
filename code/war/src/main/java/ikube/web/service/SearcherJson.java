package ikube.web.service;

import ikube.model.Search;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static ikube.IConstants.*;

/**
 * Path looks like this: http://localhost:9080/ikube/service/search/json/xxx
 *
 * @author Michael couck
 * @version 01.00
 * @since 21-01-2012
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
    @Api(description = "Simple search method, only the basic parameters and options, one field, one search string, etc.")
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults) {
        Object results = searcherService.search(
                indexName,
                split(searchStrings),
                split(searchFields),
                fragment,
                firstResult,
                maxResults);
        return buildJsonResponse(results);
    }

    /**
     * {@inheritDoc}
     */
    @GET
    @Override
    @Path(SearcherJson.SORTED)
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = SORT_FIELDS) final String sortFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults) {
        Object results = searcherService.search(
                indexName,
                split(searchStrings),
                split(searchFields),
                split(sortFields),
                fragment,
                firstResult,
                maxResults);
        return buildJsonResponse(results);
    }

    /**
     * {@inheritDoc}
     */
    @GET
    @Override
    @Path(SearcherJson.SORTED_TYPED)
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = TYPE_FIELDS) final String typeFields,
            @QueryParam(value = SORT_FIELDS) final String sortFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults) {
        Object results = searcherService.search(
                indexName,
                split(searchStrings),
                split(searchFields),
                split(typeFields),
                split(sortFields),
                fragment,
                firstResult,
                maxResults);
        return buildJsonResponse(results);
    }

    /**
     * {@inheritDoc}
     */
    @GET
    @Override
    @Path(SearcherJson.GEOSPATIAL)
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = TYPE_FIELDS) final String typeFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults,
            @QueryParam(value = DISTANCE) final int distance,
            @QueryParam(value = LATITUDE) final double latitude,
            @QueryParam(value = LONGITUDE) final double longitude) {
        Object results = searcherService.search(
                indexName,
                split(searchStrings),
                split(searchFields),
                split(typeFields),
                fragment,
                firstResult,
                maxResults,
                distance,
                latitude,
                longitude);
        return buildJsonResponse(results);
    }

    /**
     * {@inheritDoc}
     */
    @POST
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(
            @Context final HttpServletRequest request,
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
    public Response searchAll(
            @Context final HttpServletRequest request,
            @Context final UriInfo uriInfo) {
        Search search = unmarshall(Search.class, request);
        Object results = searcherService.searchAll(search);
        return buildJsonResponse(results);
    }

}