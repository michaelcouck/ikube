package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Api;
import ikube.model.ApiMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael couck
 * @version 01.00
 * @since 11-07-2014
 */
public class ApiDocsTest extends AbstractTest {

    @Mock
    ikube.web.service.Api apiMethodAnnotation;
    @Mock
    private ApiMethod apiMethod;
    @Spy
    @InjectMocks
    private ApiDocs apiDocs;

    private Method method;

    @Before
    public void before() throws NoSuchMethodException {
        method = ApiDocs.class.getDeclaredMethod("apis");
    }

    @Test
    public void api() throws Exception {
        Response response = apiDocs.api(Analyzer.class.getName());
        Api api = (Api) response.getEntity();
        assertEquals(Analyzer.class.getSimpleName(), api.getApi());
        assertEquals(9, api.getApiMethods().size());

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
        Object entity = response.getEntity();
        ArrayList apis = IConstants.GSON.fromJson(entity.toString(), ArrayList.class);
        assertEquals(7, apis.size());
    }

    @Test
    public void setMethodPath() throws NoSuchMethodException {
        when(apiMethodAnnotation.uri()).thenReturn(ApiDocs.APIS);
        apiDocs.setMethodPath(apiMethod, method, ApiDocs.APIS, apiMethodAnnotation);
        verify(apiMethod, atLeastOnce()).setUri(ApiDocs.APIS);

        when(apiMethodAnnotation.uri()).thenReturn(null);
        apiDocs.setMethodPath(apiMethod, method, ApiDocs.APIS, apiMethodAnnotation);
        verify(apiMethod, atLeastOnce()).setUri(ApiDocs.APIS + ApiDocs.APIS);
    }

    @Test
    public void setMethodType() {
        when(apiMethodAnnotation.type()).thenReturn(POST.class.getSimpleName());
        apiDocs.setMethodType(apiMethod, method, apiMethodAnnotation);
        verify(apiMethod, atLeastOnce()).setMethod(POST.class.getSimpleName());

        // We'll use the real annotation for the package name, the proxy is not suitable
        apiMethodAnnotation = method.getAnnotation(ikube.web.service.Api.class);
        apiDocs.setMethodType(apiMethod, method, apiMethodAnnotation);
        verify(apiMethod, atLeastOnce()).setMethod(GET.class.getName());
    }

    @Test
    public void setConsumesAndProducesTypes() throws NoSuchMethodException {
        apiDocs.setConsumesAndProducesTypes(apiMethod, method, MediaType.MEDIA_TYPE_WILDCARD, MediaType.APPLICATION_ATOM_XML);
        verify(apiMethod, atLeastOnce()).setConsumesType(Arrays.deepToString(new String[]{MediaType.TEXT_PLAIN}));
        verify(apiMethod, atLeastOnce()).setProducesType(Arrays.deepToString(new String[]{MediaType.APPLICATION_JSON}));

        method = ApiDocs.class.getDeclaredMethod("api", String.class);
        apiDocs.setConsumesAndProducesTypes(apiMethod, method, MediaType.MEDIA_TYPE_WILDCARD, MediaType.APPLICATION_ATOM_XML);
        verify(apiMethod, atLeastOnce()).setConsumesType(MediaType.MEDIA_TYPE_WILDCARD);
        verify(apiMethod, atLeastOnce()).setProducesType(MediaType.APPLICATION_ATOM_XML);
    }

    @Test
    public void setConsumesAndProduces() throws InstantiationException, IllegalAccessException, NoSuchMethodException {
        apiMethod = new ApiMethod();
        method = ApiDocs.class.getDeclaredMethod("apis");
        apiMethodAnnotation = method.getAnnotation(ikube.web.service.Api.class);
        apiDocs.setConsumes(apiMethod, method, apiMethodAnnotation);
        apiDocs.setProduces(apiMethod, method, apiMethodAnnotation);

        apiMethod = new ApiMethod();
        method = ApiDocs.class.getDeclaredMethod("api", String.class);
        apiMethodAnnotation = method.getAnnotation(ikube.web.service.Api.class);
        apiDocs.setConsumes(apiMethod, method, apiMethodAnnotation);
        apiDocs.setProduces(apiMethod, method, apiMethodAnnotation);
    }

}