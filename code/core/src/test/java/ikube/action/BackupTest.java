package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mockit;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackupTest extends ATest {

	private Backup backup;

	public BackupTest() {
		super(BackupTest.class);
	}

	@Before
	public void before() throws Exception {
		backup = new Backup();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void execute() throws Exception {
		File latestIndexDirectory = createIndex(INDEX_CONTEXT);

		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class);
		backup.execute(INDEX_CONTEXT);
		Mockit.tearDownMocks();

		File backupDirectory = new File(IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT));
		assertTrue(backupDirectory.exists());
		File latestBackupDirectory = new File(backupDirectory, latestIndexDirectory.getName());
		File latestServerBackupIndexDirectory = new File(latestBackupDirectory, IP);
		Directory directory = null;
		try {
			directory = FSDirectory.open(latestServerBackupIndexDirectory);
			assertTrue(IndexReader.indexExists(directory));
		} finally {
			if (directory != null) {
				directory.close();
			}
		}
	}

}
