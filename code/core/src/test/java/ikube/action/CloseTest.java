package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.cluster.IClusterManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.search.MultiSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class CloseTest extends AbstractTest {

	private Close close;

	public CloseTest() {
		super(CloseTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		close = new Close();
		IClusterManager clusterManager = Mockito.mock(IClusterManager.class);
		Deencapsulation.setField(close, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void execute() throws Exception {
		MultiSearcher multiSearcher = Mockito.mock(MultiSearcher.class);
		indexContext.setMultiSearcher(multiSearcher);
		boolean closed = close.execute(indexContext);
		assertTrue("The index was open and it should have been closed in the action : ", closed);
		Mockito.verify(indexContext, Mockito.atLeastOnce()).setMultiSearcher(Mockito.any(MultiSearcher.class));
	}

}