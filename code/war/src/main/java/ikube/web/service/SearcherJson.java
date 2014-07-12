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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
@Api(description = "The Json search rest resource")
public class SearcherJson extends Searcher {

    public static final String JSON = "/json";

    /**
     * {@inheritDoc}
     */
    @GET
    @Override
    @Path(SearcherJson.SIMPLE)
    @Api(type = "GET",
            uri = "/ikube/service/search/json/simple",
            description ="This is a simple search method, taking just one field and one search string, returning an " +
                    "array list of hash maps that represent the results. Please refer to the documentation for the format " +
                    "of the results",
            consumes = String.class,
            produces = ArrayList.class)
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults) {
        ArrayList<HashMap<String, String>> results = searcherService.search(
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
    @Api(type = "GET",
            uri = "/ikube/service/search/json/sorted",
            description ="This is a simple search method, taking just one field and one search string, returning an " +
                    "array list of hash maps that represent the results. Additionally this method supports a field in the " +
                    "that the results can be sorted on.",
            consumes = String.class,
            produces = ArrayList.class)
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = SORT_FIELDS) final String sortFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults) {
        ArrayList<HashMap<String, String>> results = searcherService.search(
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
    @Api(type = "GET",
            uri = "/ikube/service/search/json/sorted-typed",
            description ="This is a simple search method, taking just one field and one search string, returning an " +
                    "array list of hash maps that represent the results. Additionally this method supports a field in the " +
                    "that the results can be sorted on, and specifying the types of the fields directly rather than by " +
                    "inspection.",
            consumes = String.class,
            produces = ArrayList.class)
    public Response search(
            @QueryParam(value = INDEX_NAME) final String indexName,
            @QueryParam(value = SEARCH_STRINGS) final String searchStrings,
            @QueryParam(value = SEARCH_FIELDS) final String searchFields,
            @QueryParam(value = TYPE_FIELDS) final String typeFields,
            @QueryParam(value = SORT_FIELDS) final String sortFields,
            @QueryParam(value = FRAGMENT) final boolean fragment,
            @QueryParam(value = FIRST_RESULT) final int firstResult,
            @QueryParam(value = MAX_RESULTS) final int maxResults) {
        ArrayList<HashMap<String, String>> results = searcherService.search(
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
    @Api(type = "GET",
            uri = "/ikube/service/search/json/sorted",
            description ="This is a simple search method, taking just one field and one search string, returning an " +
                    "array list of hash maps that represent the results. Additionally this method supports a field in the " +
                    "that the results can be sorted on and importantly this method defines the geospatial search. Adding " +
                    "a latitude and longitude to the parameter list, as well as a point of origin, and a maximum distance, " +
                    "the results will be around a point, nad sorted by distance from that point, and not further than the " +
                    "distance from the origin specified.",
            consumes = String.class,
            produces = ArrayList.class)
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
        ArrayList<HashMap<String, String>> results = searcherService.search(
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
    @Api(type = "POST",
            uri = "/ikube/service/search/json",
            description ="This method is the recommended search method, taking a complex object that can be defined " +
                    "by the caller, with all the options available for search, and returning the search object with the " +
                    "results.",
            consumes = Search.class,
            produces = Search.class)
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
    @Api(type = "POST",
            uri = "/ikube/service/search/json",
            description ="This method is the recommended search method, taking a complex object that can be defined " +
                    "by the caller, with all the options available for search, and returning the search object with the " +
                    "results. One difference is that it will search every field in every index defined in the system. Obviously " +
                    "this is a very expensive method, and typically will not be exposed to clients, only for administrative " +
                    "purposes.",
            consumes = Search.class,
            produces = Search.class)
    public Response searchAll(
            @Context final HttpServletRequest request,
            @Context final UriInfo uriInfo) {
        Search search = unmarshall(Search.class, request);
        Object results = searcherService.searchAll(search);
        return buildJsonResponse(results);
    }

}