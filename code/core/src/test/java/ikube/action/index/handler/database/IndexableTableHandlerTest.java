package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.mock.DatabaseUtilitiesMock;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-11-2010
 */
public class IndexableTableHandlerTest extends AbstractTest {

    @Before
    public void before() {
        Mockit.setUpMocks(DatabaseUtilitiesMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(DatabaseUtilitiesMock.class);
    }

    @Test
    public void handleIndexableForked() {

    }

}