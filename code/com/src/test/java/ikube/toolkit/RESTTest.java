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
        // REST.doPost("http://mec-nft-app5.clear2pay.com:8090/SHARPy/rest/projects/", "CS_CLIENT", Object.class);
        Client client = Client.create();
        WebResource webResource = client.resource("http://mec-nft-app5.clear2pay.com:8090/SHARPy/rest/projects/");
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("project", "CS_CLIENT");
        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);
        System.out.println(response);
    }

    @Test
    public void create() {
        //String url = "http://mec-nft-app5.clear2pay.com:8090/SHARPy/rest";
        //javax.ws.rs.client.Client client = ClientBuilder.newClient().register(new HTTPBasicAuthFilter("admin", "password"));
        //WebTarget targetProject = client.target(url).path("projects/");
        //MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        //formData.add("project", "CS_CLIENT");
        //Response respProject = targetProject.request().post(Entity.form(formData));
        //System.out.println(respProject);
    }

}
