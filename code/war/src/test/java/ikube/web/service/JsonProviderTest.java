package ikube.web.service;

import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.toolkit.OBJECT;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.lang.annotation.Annotation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Michael couck
 * @version 01.00
 * @since 16-08-2014
 */
public class JsonProviderTest extends AbstractTest {

    @Spy
    @InjectMocks
    private JsonProvider jsonProvider;

    @Test
    public void readFrom() throws IOException {
        Search search = OBJECT.populateFields(new Search(), Boolean.TRUE, 3);
        InputStream inputStream = new ByteArrayInputStream(IConstants.GSON.toJson(search).getBytes());

        Search response = (Search) jsonProvider.readFrom(Object.class, Search.class, new Annotation[0],
                MediaType.WILDCARD_TYPE, new MultivaluedMapImpl(), inputStream);
        assertNotNull(response);
        assertEquals(search.getIndexName(), response.getIndexName());
    }

    @Test
    public void writeTo() throws IOException {
        Search search = OBJECT.populateFields(new Search(), Boolean.TRUE, 3);
        OutputStream outputStream = new ByteArrayOutputStream();

        MultivaluedMap<String, Object> multivaluedMap = new OutBoundHeaders();
        jsonProvider.writeTo(search, Object.class, Search.class, new Annotation[0],
                MediaType.WILDCARD_TYPE, multivaluedMap, outputStream);
        logger.error(outputStream.toString());
        Search response = IConstants.GSON.fromJson(outputStream.toString(), Search.class);
        assertNotNull(response);
        assertEquals(search.getIndexName(), response.getIndexName());
    }

}
