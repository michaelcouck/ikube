package ikube.web.service;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

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
                "saur kraut|berlin", //
                "name|name", //
                "string|string", //
                "true", //
                "0", //
                "10", //
                "20", //
                "-33.9693580", //
                "18.4622110"};
        NameValuePair[] params = getNameValuePairs(names, values);

        GetMethod getMethod = new GetMethod(url.toString());
        getMethod.setQueryString(params);
        HttpClient httpClient = new HttpClient();

        int result = httpClient.executeMethod(getMethod);
        String body = getMethod.getResponseBodyAsString();
        System.out.println("Response code : " + result + ", body : " + body);
    }

    private NameValuePair[] getNameValuePairs(String[] names, String[] values) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (int i = 0; i < names.length && i < values.length; i++) {
            NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
            nameValuePairs.add(nameValuePair);
        }
        return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
    }

}