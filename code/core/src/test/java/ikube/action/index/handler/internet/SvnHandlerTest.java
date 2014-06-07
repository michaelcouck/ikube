package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableSvn;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ForkJoinTask;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
@RunWith(MockitoJUnitRunner.class)
public class SvnHandlerTest extends AbstractTest {

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = SvnResourceProvider.class)
    public static class SvnResourceProviderMock {

        @Mock
        void $init(final IndexableSvn indexableSvn) throws Exception {
            // Do nothing
        }

    }

    private SvnHandler svnHandler;
    @org.mockito.Mock
    private IndexableSvn indexableSvn;
    @org.mockito.Mock
    private SvnResourceHandler svnResourceHandler;

    @Before
    public void before() {
        svnHandler = new SvnHandler();
        Mockit.setUpMocks(SvnResourceProviderMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(SvnResourceProvider.class);
    }

    @Test
    public void handleIndexable() throws Exception {
        IndexableSvn indexableSvn = Mockito.mock(IndexableSvn.class);
        ForkJoinTask forkJoinTask = svnHandler.handleIndexableForked(indexContext, indexableSvn);
        Assert.assertNotNull(forkJoinTask);
    }

    @Test
    public void handleResource() throws Exception {
        svnHandler.handleResource(indexContext, indexableSvn, null);
    }

}