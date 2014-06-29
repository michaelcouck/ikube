package ikube.web.toolkit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import ikube.toolkit.SerializationUtilities;

import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is an example in Java of how to access the rest web service to get results from the GeoSpatial index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-03-2012
 */
public class SearcherClient {

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) throws Exception {
        int port = 80;
        String host = "ikube.be";
        String path = "/ikube/service/search/xml/geospatial";
        String url = new URL("http", host, port, path).toString();
        String username = "user";
        String password = "user";

        String[] names = {
                "indexName",
                "searchStrings",
                "searchFields",
                "typeFields",
                "fragment",
                "firstResult",
                "maxResults",
                "distance",
                "latitude",
                "longitude"};
        String[] values = {
                "geospatial",
                "cape town",
                "name",
                "string",
                "true",
                "0",
                "10",
                "10",
                "-33.9693580",
                "18.4622110"};

        // Create the client to query the rest api
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(username, password));
        WebResource webResource = client.resource(url);
        // Set the parameters for the index and the search strings etc.
        for (int i = 0; i < names.length; i++) {
            webResource = webResource.queryParam(names[i], values[i]);
        }
        // Query the server
        String response = webResource.accept(MediaType.WILDCARD).type(MediaType.WILDCARD).get(String.class);
        // De-serialize the xml into the results list
        ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(response);

        System.out.println("Response : " + response);
        System.out.println("Search results : " + results);
    }

}