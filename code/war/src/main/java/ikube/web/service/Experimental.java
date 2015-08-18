package ikube.web.service;

import ikube.experimental.Searcher;
import ikube.model.Search;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

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
@Path(Experimental.EXPERIMENTAL)
@Api(description = "The Json search rest resource")
public class Experimental extends Resource {

    static final String EXPERIMENTAL = "/experimental";

    @Autowired
    @Qualifier("searchers")
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private Map<String, ikube.experimental.Searcher> searchers;

    /**
     * {@inheritDoc}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Api(description = "Bla...", produces = Search.class)
    public Response search(final Search search) throws IOException, ParseException {
        String fieldName = search.getSearchFields().get(0);
        String searchString = search.getSearchStrings().get(0);
        Searcher searcher = searchers.get(search.getIndexName());
        if (searcher == null) {
            return buildResponse("No searcher defined for : " + search.getIndexName());
        }
        return buildResponse(searcher.doSearch(fieldName, searchString));
    }

}