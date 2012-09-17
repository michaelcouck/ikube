package ikube.action;

import ikube.ATest;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class OptimizerTest extends ATest {

	@Cascading
	private IClusterManager clusterManager;
	/** Class under test. */
	private Optimizer optimizer;

	public OptimizerTest() {
		super(OptimizerTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		optimizer = new Optimizer();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@Ignore
	public void executeInternal() {
		IndexContext<?> indexContext = new IndexContext<Object>();
		Deencapsulation.setField(optimizer, "clusterManager", clusterManager);
		logger.info("Index context : " + indexContext);
		indexContext.setIndexDirectoryPath("/tmp/plwiki/index");
		indexContext.setBufferedDocs(1000);
		indexContext.setBufferSize(256);
		indexContext.setCompoundFile(true);
		indexContext.setMaxFieldLength(10000);
		indexContext.setMaxReadLength(1000000);
		indexContext.setMergeFactor(1000);

		optimizer.executeInternal(indexContext);
	}

}