package ikube.toolkit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

// import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
// import javax.ws.rs.core.Response;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07-09-2015
 */
@Ignore
public class RESTTest {

    @Test
    public void rest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource("http://your-url-here:8090/path/to/rest/service");
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("parameter-one", "parameter-value-one");
        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);
        System.out.println(response);
    }

}
