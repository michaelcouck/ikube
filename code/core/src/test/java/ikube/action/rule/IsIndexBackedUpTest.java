package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.action.index.IndexManager;
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
public class IsIndexBackedUpTest extends AbstractTest {

	private IsIndexBackedUp isIndexBackedUp;

	public IsIndexBackedUpTest() {
		super(IsIndexBackedUpTest.class);
	}

	@Before
	public void before() {
		isIndexBackedUp = new IsIndexBackedUp();
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void evaluate() throws IOException {
		File latestIndexDirectory = createIndexFileSystem(indexContext, "some strings");
		boolean result = isIndexBackedUp.evaluate(indexContext);
		assertFalse(result);

		String indexDirectoryBackupPath = IndexManager.getIndexDirectoryPathBackup(indexContext) + "/"
				+ latestIndexDirectory.getParentFile().getName();
		File indexDirectoryBackup = new File(indexDirectoryBackupPath);
		FileUtils.copyDirectoryToDirectory(latestIndexDirectory, indexDirectoryBackup);

		result = isIndexBackedUp.evaluate(indexContext);
		assertTrue(result);
	}

}