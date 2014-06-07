package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableSvn;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
public class SvnHandlerTest extends AbstractTest {

    private SvnHandler svnHandler;

    @Before
    public void before() {
        svnHandler = new SvnHandler();
    }

    @After
    public void after() {
    }

    @Test
    @Ignore
    public void handleIndexable() throws Exception {
        IndexableSvn indexableSvn = Mockito.mock(IndexableSvn.class);
        svnHandler.handleIndexableForked(indexContext, indexableSvn);
    }

}