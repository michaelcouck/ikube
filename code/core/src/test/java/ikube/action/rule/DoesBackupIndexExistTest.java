package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DoesBackupIndexExistTest extends ATest {

	public DoesBackupIndexExistTest() {
		super(DoesBackupIndexExistTest.class);
	}

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void execute() {
		boolean backupIndexExists = new DoesBackupIndexExist().evaluate(INDEX_CONTEXT);
		assertFalse("There is no backup index yet : ", backupIndexExists);

		// Create a backup index
		String indexDirectoryPathBackup = INDEX_CONTEXT.getIndexDirectoryPathBackup();
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn(indexDirectoryPathBackup);
		createIndex(INDEX_CONTEXT, "Some data");
		backupIndexExists = new DoesBackupIndexExist().evaluate(INDEX_CONTEXT);
		assertTrue("The backup index should exist : ", backupIndexExists);
	}

}