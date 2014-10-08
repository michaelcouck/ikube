package ikube.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

/**
 * This is an example in Java of how to access the rest web service to get results from a search(Lucene)
 * index. In this case it is the GeoSpatial index. We need to specify the fields to search and the search string(s),
 * in this case just one. The GeoSpatial index is, well obviously enriched with geospatial data, so we can search
 * the index and have the results narrowed to an area around a particular point, and the results will be sorted
 * according to distance from the point we specify.
 * <p/>
 * The results are in Json, but as easily have been in XML using the XML service instead of the Json service.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-03-2012
 */
public class Searcher extends Base {

    public static void main(final String[] args) throws Exception {
        String url = "http://ikube.be/ikube/service/search/json";

        // The search object that we sill convert to Json with all the parameters that
        // we need like the name of the index to query, the search strings and types and so on
        Search search = new Search();

        search.indexName = "geospatial";
        search.searchStrings = Arrays.asList("cape town");
        search.typeFields = Arrays.asList("string");
        search.searchFields = Arrays.asList("name");
        search.occurrenceFields = Arrays.asList("must");
        search.coordinate = new Coordinate(-33.9693580, 18.4622110);

        search.distance = 10;
        search.firstResult = 0;
        search.maxResults = 10;
        search.fragment = Boolean.TRUE;
        search.distributed = Boolean.TRUE;

        // Convert the search object to Json
        Gson gson = new GsonBuilder().create();
        String body = gson.toJson(search);

        ClientConfig clientConfig = new DefaultClientConfig();
        // We can add a writer for Jersey for the search object, and add it
        // to the client configuration here if necessary, and then we don't have to
        // convert the object to Json using Gson
        Client client = Client.create(clientConfig);
        WebResource webResource = client.resource(url);

        // Query the server
        String response = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(String.class, body);

        System.out.println("Response : " + response);
    }

}