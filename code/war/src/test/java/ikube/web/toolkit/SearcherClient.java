package ikube.web.toolkit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        String path = "/ikube/service/search/xml/geospatial";
        String url = new URL("http", host, port, path).toString();

        String[] names = {"indexName", "searchStrings", "searchFields", "typeFields", "fragment", "firstResult", "maxResults", "distance", "latitude", "longitude"};
        String[] values = {"geospatial", "cape town", "name", "string", "true", "0", "10", "10", "-33.9693580", "18.4622110"};
        NameValuePair[] params = getNameValuePairs(names, values);

        GetMethod getMethod = new GetMethod(url);
        getMethod.setQueryString(params);
        HttpClient httpClient = new HttpClient();

        int result = httpClient.executeMethod(getMethod);
        String results = getMethod.getResponseBodyAsString();
        System.out.println("Result : " + result);
        System.out.println("Results : " + results);
    }

    private static NameValuePair[] getNameValuePairs(String[] names, String[] values) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (int i = 0; i < names.length && i < values.length; i++) {
            NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
            nameValuePairs.add(nameValuePair);
        }
        return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
    }

}