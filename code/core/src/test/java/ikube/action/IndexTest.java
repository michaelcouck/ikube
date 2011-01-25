package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.cluster.IClusterManager;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexTest extends BaseTest {

	private Index index = new Index();

	@Before
	public void before() {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.setWorking(indexContext.getIndexName(), "", Boolean.FALSE);
	}

	@Test
	public void execute() throws Exception {
		long maxAge = indexContext.getMaxAge();

		indexContext.setMaxAge(0);

		boolean done = index.execute(indexContext);
		assertTrue(done);

		indexContext.setMaxAge(maxAge);

		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

}
