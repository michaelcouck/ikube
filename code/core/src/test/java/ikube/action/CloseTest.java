package ikube.action;

import ikube.AbstractTest;
import ikube.cluster.IClusterManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.10
 */
@SuppressWarnings("deprecation")
public class CloseTest extends AbstractTest {

    private Close close;

    @Before
    public void before() {
        Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
        close = new Close();
        IClusterManager clusterManager = Mockito.mock(IClusterManager.class);
        Deencapsulation.setField(close, clusterManager);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
    }

    @Test
    public void execute() throws Exception {
        IndexSearcher multiSearcher = Mockito.mock(IndexSearcher.class);
        indexContext.setMultiSearcher(multiSearcher);
        boolean closed = close.execute(indexContext);
        assertTrue("The index was open and it should have been closed in the action : ", closed);
        Mockito.verify(indexContext, Mockito.atLeastOnce()).setMultiSearcher(Mockito.any(IndexSearcher.class));
    }

}