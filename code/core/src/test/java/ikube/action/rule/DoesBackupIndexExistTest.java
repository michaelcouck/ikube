package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DoesBackupIndexExistTest extends AbstractTest {

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void execute() {
		boolean backupIndexExists = new DoesBackupIndexExist().evaluate(indexContext);
		assertFalse("There is no backup index yet : ", backupIndexExists);

		// Create a backup index
		String indexDirectoryPathBackup = indexContext.getIndexDirectoryPathBackup();
		when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPathBackup);
		createIndexFileSystem(indexContext, "Some data");
		backupIndexExists = new DoesBackupIndexExist().evaluate(indexContext);
		assertTrue("The backup index should exist : ", backupIndexExists);
	}

}