package ikube.web.service;

import ikube.AbstractTest;
import ikube.model.Analysis;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        String content = Arrays.deepToString((Object[]) entity);

        assertTrue("Must have the weird characters : ", content.contains("ä"));
        assertTrue("Must have the weird characters : ", content.contains(somthingElseAlToGether));

        Analysis<String, String> analysis = populateFields(new Analysis(), Boolean.TRUE, 100, "exception");
        response = resource.buildResponse(analysis);
        entity = response.getEntity();
        assertTrue(Analysis.class.isAssignableFrom(entity.getClass()));
    }

    @Test
    public void split() {
        String searchString = "hello, world | there you are";
        String[] result = resource.split(searchString);
        assertEquals("hello, world ", result[0]);
        assertEquals(" there you are", result[1]);
    }

}