package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import mockit.Mockit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class IsIndexBackedUpTest extends ATest {

	private IsIndexBackedUp isIndexBackedUp;

	public IsIndexBackedUpTest() {
		super(IsIndexBackedUpTest.class);
	}

	@Before
	public void before() {
		isIndexBackedUp = new IsIndexBackedUp();
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void evaluate() throws IOException {
		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "some strings");
		boolean result = isIndexBackedUp.evaluate(INDEX_CONTEXT);
		assertFalse(result);

		File indexDirectoryBackup = new File(IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT));
		FileUtils.copyDirectoryToDirectory(latestIndexDirectory, indexDirectoryBackup);

		result = isIndexBackedUp.evaluate(INDEX_CONTEXT);
		assertTrue(result);
	}

}