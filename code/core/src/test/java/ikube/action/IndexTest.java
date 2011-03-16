package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.cluster.IClusterManager;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexTest extends BaseTest {

	@SuppressWarnings("unused")
	private transient final Index index = new Index();

	@Before
	public void before() {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.setWorking(indexContext.getIndexName(), "", Boolean.FALSE);
	}

	@Test
	public void execute() throws Exception {
		// TODO This test must be re-done with mocking
		assertTrue(true);
	}

}
