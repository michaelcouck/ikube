package ikube.web.service;

import ikube.toolkit.HttpClientUtilities;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URL;

/**
 * This is an example in Java of how to access the rest web service to get results from the GeoSpatial index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-03-2012
 */
public class SearcherClientIntegration {

    @Test
    @SuppressWarnings("StringBufferReplaceableByString")
    public void main() throws Exception {

        String path = "/ikube/service/search/xml/geospatial";
        URL url = new URL("http", "ikube.be", 80, path);
        System.out.println(url);

        String[] names = { //
                "indexName", //
                "searchStrings", //
                "searchFields", //
                "typeFields", //
                "fragment", //
                "firstResult", //
                "maxResults", //
                "distance", //
                "latitude", //
                "longitude"};
        String[] values = { //
                "geospatial", //
                "hotel", //
                "name", //
                "string", //
                "true", //
                "0", //
                "10", //
                "20", //
                "-33.9693580", //
                "18.4622110"};

        String response = HttpClientUtilities.doGet(
                url.toString(),
                null,
                null,
                names,
                values,
                MediaType.WILDCARD,
                MediaType.WILDCARD,
                String.class);
        System.out.println("Response body : " + response);
    }

}