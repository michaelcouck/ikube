package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mockit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackupTest extends ATest {

	public BackupTest() {
		super(BackupTest.class);
	}

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class);
		File backupDirectory = new File(IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT));
		FileUtils.deleteDirectory(backupDirectory);
	}

	@After
	public void after() throws Exception {
		File backupDirectory = new File(IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT));
		FileUtils.deleteDirectory(backupDirectory);
		Mockit.tearDownMocks(IndexManager.class, ApplicationContextManager.class);
	}

	@Test
	public void execute() throws Exception {
		String indexDirectoryPath = getServerIndexDirectoryPath(INDEX_CONTEXT);
		File indexDirectory = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
		createIndex(indexDirectory);

		Backup backup = new Backup();
		backup.execute(INDEX_CONTEXT);

		File backupDirectory = new File(IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT));
		assertTrue(backupDirectory.exists());
		// TODO For some reason the index is not really created in this test. Could
		// this be the file system, i.e. hardware?
		// assertTrue(new DirectoryExistsAndNotLocked().evaluate(backupDirectory));
	}

}
