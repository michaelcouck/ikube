package ikube.toolkit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import ikube.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;

/**
 * TODO: Migrate to {@link com.sun.jersey.api.client.Client}. Document... Write tests...
 * TODO: Done, just the documentation and the tests ;)
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public class REST {

    static final Logger LOGGER = LoggerFactory.getLogger(REST.class);

    public static <T> T doGet(
            final String url,
            final Type returnType) {
        return doGet(url, null, null, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doGet(
            final String url,
            final String[] names,
            final String[] values,
            final Type returnType) {
        return doGet(url, null, null, names, values, returnType);
    }

    public static <T> T doGet(
            final String url,
            final String username,
            final String password,
            final String[] names,
            final String[] values,
            final Type returnType) {
        return doGet(url, username, password, names, values, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doGet(
            final String url,
            final String username,
            final String password,
            final String[] names,
            final String[] values,
            final String consumes,
            final String produces,
            final Type returnType) {
        Client client = Client.create();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            client.addFilter(new HTTPBasicAuthFilter(username, password));
        }
        WebResource webResource = client.resource(url);
        if (names != null && values != null) {
            webResource = setParameters(webResource, names, values);
        }
        String response = webResource.accept(consumes).type(produces).get(String.class);
        if (Class.class.isAssignableFrom(returnType.getClass())) {
            if (String.class.isAssignableFrom((Class) returnType)) {
                return (T) response;
            }
        }
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
        return doPost(url, null, null, entity, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, names, values, returnType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T doPost(
            final String url,
            final String username,
            final String password,
            final Object entity,
            final String consumes,
            final String produces,
            final String[] names,
            final String[] values,
            final Class<T> returnType) {
        Client client = Client.create();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            client.addFilter(new HTTPBasicAuthFilter(username, password));
        }
        String body;
        if (entity == null || String.class.isAssignableFrom(entity.getClass())) {
            body = (String) entity;
        } else {
            body = Constants.GSON.toJson(entity);
        }
        WebResource webResource = client.resource(url);
        if (names != null && values != null) {
            webResource = setParameters(webResource, names, values);
        }
        // Potentially we could send the entire entity to the rest service,
        // but strangely enough Jersey doesn't like to do the de-serialization
        // all on it's own, it wants some kind of body mapper, go figure, so in this
        // case, and all others we send the string body, not the entity it's self
        String response = webResource
                .accept(consumes)
                .type(produces)
                .post(String.class, body);
        if (String.class.isAssignableFrom(returnType)) {
            return (T) response;
        }
        return Constants.GSON.fromJson(response, returnType);
    }

    private static WebResource setParameters(
            final WebResource webResource,
            final String[] names,
            final String[] values) {
        WebResource withParams = webResource;
        for (int i = 0; i < names.length; i++) {
            withParams = withParams.queryParam(names[i], values[i]);
        }
        return withParams;
    }

}
