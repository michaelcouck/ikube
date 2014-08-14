package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Persistable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections.Reflections;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-11-2012
 */
public class ResourceTest extends AbstractTest {

    private Resource resource;

    @Before
    public void before() {
        resource = new SearcherJson();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void buildResponse() {
        String russian = "Россия   русский язык  ";
        String german = "Produktivität";
        String french = "Qu'est ce qui détermine la productivité, et comment est-il mesuré?";
        String somthingElseAlToGether = "Soleymān Khāţer";
        String[] result = {russian, german, french, somthingElseAlToGether};
        Response response = resource.buildResponse(result);
        Object entity = response.getEntity();
        logger.info("Entity : " + entity);
        logger.info("Entity : " + Arrays.deepToString(result));
        assertTrue("Must have the weird characters : ", entity.toString().contains(somthingElseAlToGether));
        assertTrue("Must have the weird characters : ", entity.toString().contains("ä"));

        Analysis<String, String> analysis = populateFields(new Analysis(), Boolean.TRUE, 100, "exception");
        response = resource.buildResponse(analysis);
        logger.info("Response : " + response.getEntity());
    }

    @Test
    public void unmarshall() throws Exception {
        Set<Class<? extends Persistable>> classes = new Reflections(Persistable.class.getPackage().getName()).getSubTypesOf(Persistable.class);

        for (final Class<? extends Persistable> clazz : classes) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            Persistable persistable = populateFields(clazz.newInstance(), Boolean.FALSE, 10);
            final String json = IConstants.GSON.toJson(persistable);
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(json.getBytes());

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            ServletInputStream servletInputStream = mock(ServletInputStream.class);

            when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
            when(servletInputStream.read(any(byte[].class))).thenAnswer(new Answer<Integer>() {
                @Override
                public Integer answer(final InvocationOnMock invocation) throws Throwable {
                    byte[] bytes = (byte[]) invocation.getArguments()[0];
                    return arrayInputStream.read(bytes);
                }
            });

            resource.unmarshall(clazz, httpServletRequest);
        }
    }

    @Test
    public void split() {
        String searchString = "hello, world | there you are";
        String[] result = resource.split(searchString);
        assertEquals("hello, world ", result[0]);
        assertEquals(" there you are", result[1]);
    }

}