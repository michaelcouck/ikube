package ikube.toolkit;

import ikube.AbstractTest;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just has to run without exception, the number of index files that are
 * merged seems to be random, and Lucene decides, strangely enough.
 * 
 * @author Michael Couck
 * @since 08-02-2013
 * @version 01.00
 */
public class OptimizerTest extends AbstractTest {

	/** "192.168.1.2", "192.168.1.3" */
	private String[] ips = { "192.168.1.1" };

	@Before
	public void before() {
		createIndexesFileSystem(indexContext, System.currentTimeMillis(), ips, "and a little data");
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void main() {
		for (final String ip : ips) {
			File indexDirectory = FileUtilities.findDirectoryRecursively(new File("."), ip);
			logger.info("Index directory : " + indexDirectory.getAbsolutePath());
			String[] args = { FileUtilities.cleanFilePath(indexDirectory.getAbsolutePath()) };
			Optimizer.main(args);
		}
	}

}
