package ikube;

import ikube.search.SearchToolkit;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2014
 */
public class IkubeTest extends AbstractTest {

    private static String[] PARAMETERS;

    @MockClass(realClass = SearchToolkit.class)
    public static class SearchToolkitMock {
        @Mock
        public static void main(final String[] args) {
            // This is mocked
            PARAMETERS = args;
        }
    }

    @Before
    public void before() {
        Mockit.setUpMocks(SearchToolkitMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks();
    }

    @Test
    public void main() {
        String[] parameters = new String[]{SearchToolkit.class.getName(), "parameter-one", "parameter-two"};
        Ikube.main(parameters);
        assertEquals(PARAMETERS[0], parameters[1]);
        assertEquals(PARAMETERS[1], parameters[2]);
    }

}