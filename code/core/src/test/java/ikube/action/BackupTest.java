package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Deencapsulation;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 16.01.12
 * @version 01.00
 */
public class BackupTest extends AbstractTest {

	private Backup backup;

	public BackupTest() {
		super(BackupTest.class);
	}

	@Before
	public void before() throws Exception {
		backup = new Backup();
		Deencapsulation.setField(backup, clusterManager);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
		
	}

	@After
	public void after() throws Exception {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void execute() throws Exception {
		createIndex(indexContext, "Some strings, like Michael Couck");

		backup.execute(indexContext);

		File backupDirectory = new File(IndexManager.getIndexDirectoryPathBackup(indexContext));
		assertTrue(backupDirectory.exists());
		File latestBackupDirectory = IndexManager.getLatestIndexDirectory(backupDirectory.getAbsolutePath());
		Directory directory = null;
		try {
			directory = FSDirectory.open(new File(latestBackupDirectory, ip));
			assertTrue(IndexReader.indexExists(directory));
		} finally {
			if (directory != null) {
				directory.close();
			}
		}
	}

}