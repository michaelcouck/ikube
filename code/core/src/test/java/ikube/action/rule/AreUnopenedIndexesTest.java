package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class AreUnopenedIndexesTest extends AbstractTest {

	private AreUnopenedIndexes areUnopenedIndexes;

	@Before
	public void before() {
		areUnopenedIndexes = new AreUnopenedIndexes();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void evaluate() throws Exception {
		boolean result = areUnopenedIndexes.evaluate(indexContext);
		assertFalse(result);

		File latestIndexDirectory = createIndexFileSystem(indexContext, "some words to index");
		result = areUnopenedIndexes.evaluate(indexContext);
		assertFalse(result);

		FileUtils.copyDirectory(latestIndexDirectory, new File(latestIndexDirectory.getParent(), "127.0.0.1.1234567890"));
		result = areUnopenedIndexes.evaluate(indexContext);
		assertTrue(result);
	}

}