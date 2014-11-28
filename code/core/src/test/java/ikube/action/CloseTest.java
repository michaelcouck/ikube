package ikube.action;

import ikube.AbstractTest;
import ikube.cluster.IClusterManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.mock.IndexReaderMock;
import ikube.toolkit.THREAD;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.index.IndexReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@SuppressWarnings("deprecation")
public class CloseTest extends AbstractTest {

    private Close close;

    @Before
    public void before() {
        close = new Close();
        THREAD.initialize();
        IClusterManager clusterManager = mock(IClusterManager.class);
        Deencapsulation.setField(close, clusterManager);
        Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, IndexReaderMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, IndexReader.class);
    }

    @Test
    public void execute() throws Exception {
        when(multiSearcher.getIndexReader()).thenReturn(indexReader);
        when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);

        boolean closed = close.execute(indexContext);
        THREAD.sleep(11000);

        assertTrue("The index was open and it should have been closed in the action : ", closed);
        // verify(indexReader, Mockito.atLeastOnce()).close();
        verify(multiSearcher, atLeastOnce()).getIndexReader();
    }

}