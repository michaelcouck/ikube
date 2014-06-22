package ikube.toolkit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import ikube.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

/**
 * TODO: Migrate to {@link com.sun.jersey.api.client.Client}. Document... Write tests...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public class HttpClientUtilities {

    static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtilities.class);

    public static <T> T doGet(
            final String url,
            final Class<T> returnType) {
        return doGet(url, null, null, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doGet(
            final String url,
            final String[] names,
            final String[] values,
            final Class<T> returnType) {
        return doGet(url, null, null, names, values, returnType);
    }

    public static <T> T doGet(
            final String url,
            final String username,
            final String password,
            final String[] names,
            final String[] values,
            final Class<T> returnType) {
        Client client = Client.create();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            client.addFilter(new HTTPBasicAuthFilter(username, password));
        }
        WebResource webResource = client.resource(url);
        if (names != null && values != null) {
            setParameters(webResource, names, values);
        }
        String response = webResource
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(String.class);
        return Constants.GSON.fromJson(response, returnType);
    }

    public static <T> T doPost(
            final String url,
            final Object entity,
            final Class<T> returnType) {
        return doPost(url, entity, null, null, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doPost(
            final String url,
            final Object entity,
            final String[] names,
            final String[] values,
            final Class<T> returnType) {
        return doPost(url, null, null, entity, names, values, returnType);
    }

    public static <T> T doPost(
            final String url,
            final String username,
            final String password,
            final Object entity,
            final String[] names,
            final String[] values,
            final Class<T> returnType) {
        Client client = Client.create();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            client.addFilter(new HTTPBasicAuthFilter(username, password));
        }
        WebResource webResource = client.resource(url);
        if (names != null && values != null) {
            setParameters(webResource, names, values);
        }
        String body;
        if (String.class.isAssignableFrom(entity.getClass())) {
            body = (String) entity;
        } else {
            body = Constants.GSON.toJson(entity);
        }
        String response = webResource
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(String.class, body);
        return Constants.GSON.fromJson(response, returnType);
    }

    private static void setParameters(
            final WebResource webResource,
            final String[] names,
            final String[] values) {
        for (int i = 0; i < names.length; i++) {
            webResource.queryParam(names[i], values[i]);
        }
    }

}
