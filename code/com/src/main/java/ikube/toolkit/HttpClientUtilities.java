package ikube.toolkit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import ikube.Constants;

import javax.ws.rs.core.MediaType;

/**
 * TODO: Migrate to {@link com.sun.jersey.api.client.Client}.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public class HttpClientUtilities {

    private static final Client CLIENT;

    static {
        CLIENT = Client.create();
    }

    public static <T> T doGet(final String url, final Class<T> type) {
        // CLIENT.addFilter(new HTTPBasicAuthFilter(username, password));
        return doGet(url, type, null, null);
    }

    public static <T> T doPost(final String url, final Object entity, final Class<T> type) {
        return doPost(url, entity, type, null, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doGet(final String url, final Class<T> returnType, final String[] names, final String[] values) {
        WebResource webResource = CLIENT.resource(url);
        if (names != null && values != null) {
            setParameters(webResource, names, values);
        }
        ClientResponse clientResponse = webResource
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        String response = clientResponse.getEntity(String.class);
        if (String.class.isAssignableFrom(returnType)) {
            return (T) response;
        }
        return Constants.GSON.fromJson(response, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doPost(final String url, final Object entity, final Class<T> returnType, final String[] names, final String[] values) {
        WebResource webResource = CLIENT.resource(url);
        if (names != null && values != null) {
            setParameters(webResource, names, values);
        }
        ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, Constants.GSON.toJson(entity));
        String response = clientResponse.getEntity(String.class);
        if (String.class.isAssignableFrom(returnType)) {
            return (T) response;
        }
        return Constants.GSON.fromJson(response, returnType);
    }

    private static void setParameters(final WebResource webResource, final String[] names, final String[] values) {
        for (int i = 0; i < names.length; i++) {
            webResource.queryParam(names[i], values[i]);
        }
    }

}
