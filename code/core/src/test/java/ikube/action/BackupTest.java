package ikube.action;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.toolkit.FILE;
import mockit.Deencapsulation;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @since 16.01.12
 * @version 01.00
 */
public class BackupTest extends AbstractTest {

	private Backup backup;

	@Before
	public void before() throws Exception {
		backup = new Backup();
		Deencapsulation.setField(backup, clusterManager);
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
		
	}

	@After
	public void after() throws Exception {
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	public void execute() throws Exception {
		createIndexFileSystem(indexContext, "Some strings, like Michael Couck");

		backup.execute(indexContext);

		File backupDirectory = new File(IndexManager.getIndexDirectoryPathBackup(indexContext));
		assertTrue(backupDirectory.exists());
		File latestBackupDirectory = IndexManager.getLatestIndexDirectory(backupDirectory.getAbsolutePath());
		Directory directory = null;
		try {
			directory = FSDirectory.open(new File(latestBackupDirectory, ip));
			assertTrue(DirectoryReader.indexExists(directory));
		} finally {
			if (directory != null) {
				directory.close();
			}
		}
	}

}