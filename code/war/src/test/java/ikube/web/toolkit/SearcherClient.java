package ikube.web.toolkit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import ikube.model.Coordinate;
import ikube.model.Search;
import ikube.web.service.JsonProvider;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Arrays;

/**
 * This is an example in Java of how to access the rest web service to get results from the GeoSpatial index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-03-2012
 */
public class SearcherClient {

    public static void main(final String[] args) throws Exception {
        int port = 80;
        String host = "ikube.be";
        String path = "/ikube/service/search/json";
        String url = new URL("http", host, port, path).toString();

        Search search = new Search();

        search.setIndexName("geospatial");
        search.setSearchStrings(Arrays.asList("cape town"));
        search.setTypeFields(Arrays.asList("string"));
        search.setSearchFields(Arrays.asList("name"));
        search.setOccurrenceFields(Arrays.asList("must"));
        search.setCoordinate(new Coordinate(-33.9693580, 18.4622110));

        search.setDistance(10);
        search.setFirstResult(0);
        search.setMaxResults(10);
        search.setFragment(Boolean.TRUE);
        search.setDistributed(Boolean.TRUE);

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JsonProvider.class);
        // Create the client to query the rest api
        Client client = Client.create(clientConfig);

        WebResource webResource = client.resource(url);

        // Query the server
        Search response = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(Search.class, search);

        System.out.println("Response : " + response);
    }

    @Test
    public void search() throws Exception {
        SearcherClient.main(null);
    }

}