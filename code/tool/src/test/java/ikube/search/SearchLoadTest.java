package ikube.search;

import ikube.AbstractTest;
import ikube.toolkit.REST;
import ikube.toolkit.OBJECT;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 31-08-2014
 */
public class SearchLoadTest extends AbstractTest {

    private static int EXECUTIONS;

    @Before
    public void before() {
        Mockit.setUpMocks(HttpClientUtilitiesMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(REST.class);
    }

    @Test
    public void main() throws Exception {
        SearchLoadClient searchLoad = new SearchLoadClient();
        // We just execute this because there is nothing to test in the results
        SearchLoadClient.main(new String[]{});
        logger.error("Executions : " + EXECUTIONS);
        Assert.assertTrue(searchLoad.iterations < EXECUTIONS);
    }

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = REST.class)
    public static class HttpClientUtilitiesMock {
        @Mock
        @SuppressWarnings("unchecked")
        public static <T> T doPost(final String url, final Object entity, final Class<T> returnType) {
            EXECUTIONS++;
            return (T) OBJECT.populateFields(new ikube.model.Search(), true, 3);
        }
    }

}
