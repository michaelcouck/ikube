package ikube.web.service;

import ikube.AbstractTest;
import ikube.model.Api;
import ikube.model.ApiMethod;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael couck
 * @version 01.00
 * @since 11-07-2014
 */
public class ApiDocsTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ApiDocs apiDocs;

    @Test
    public void api() throws Exception {
        Response response = apiDocs.api(Analyzer.class.getName());
        Api api = (Api) response.getEntity();
        assertEquals(Analyzer.class.getName(), api.getApi());
        assertEquals(7, api.getApiMethods().size());

        Iterator<ApiMethod> apiMethodIterator = api.getApiMethods().iterator();
        ApiMethod apiMethod = apiMethodIterator.next();
        assertNotNull(apiMethod.getConsumes());
        assertNotNull(apiMethod.getDescription());
        assertNotNull(apiMethod.getProduces());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void apis() throws Exception {
        Response response = apiDocs.apis();
        Collection<Api> apis = (Collection<Api>) response.getEntity();
        assertEquals(7, apis.size());
    }

}