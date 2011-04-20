package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class IsBackupIndexCurrentTest extends ATest {

	private IsBackupIndexCurrent isBackupIndexCurrent;

	public IsBackupIndexCurrentTest() {
		super(IsBackupIndexCurrentTest.class);
	}

	@Before
	public void before() {
		isBackupIndexCurrent = new IsBackupIndexCurrent();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void evaluate() throws IOException {
		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "some strings");
		boolean result = isBackupIndexCurrent.evaluate(INDEX_CONTEXT);
		assertFalse(result);

		File indexDirectoryBackup = new File(IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT));
		FileUtils.copyDirectoryToDirectory(latestIndexDirectory, indexDirectoryBackup);

		result = isBackupIndexCurrent.evaluate(INDEX_CONTEXT);
		assertTrue(result);

		File latestIndexDirectoryBackup = FileUtilities.getLatestIndexDirectory(INDEX_CONTEXT.getIndexDirectoryPathBackup());
		String oldDirectoryName = Long.toString(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 365));
		File latestIndexDirectoryBackupOld = new File(latestIndexDirectory.getParentFile(), oldDirectoryName);
		boolean renamed = latestIndexDirectoryBackup.renameTo(latestIndexDirectoryBackupOld);

		if (!renamed) {
			logger.warn("Couldn't rename the directory : " + latestIndexDirectoryBackup + ", " + latestIndexDirectoryBackupOld);
		}

		result = isBackupIndexCurrent.evaluate(INDEX_CONTEXT);
		assertFalse(result);
	}

}