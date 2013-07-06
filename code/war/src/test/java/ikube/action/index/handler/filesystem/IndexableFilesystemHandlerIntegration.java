package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.util.concurrent.ForkJoinTask;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemHandlerIntegration extends IntegrationTest {

	private IndexContext<?> desktop;
	private IndexableFileSystem desktopFolder;
	private IndexableFileSystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		String dataIndexFolderPath = FileUtilities.cleanFilePath(new File(".").getAbsolutePath());
		logger.info("Data folder : " + dataIndexFolderPath);
		desktop = ApplicationContextManager.getBean("desktop");
		desktopFolder = ApplicationContextManager.getBean("desktopFolder");
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFileSystemHandler.class.getName());

		desktopFolder.setPath(dataIndexFolderPath);
		// This should be true for performance testing
		desktopFolder.setUnpackZips(Boolean.FALSE);

		String ip = UriUtilities.getIp();
		IndexWriter indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), ip);
		desktop.setIndexWriters(indexWriter);

		FileUtilities.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handleIndexable() throws Exception {
		Directory directory = null;
		try {

			ForkJoinTask<?> forkJoinTask = indexableFilesystemHandler.handleIndexableForked(desktop, desktopFolder);
			ThreadUtilities.executeForkJoinTasks(desktop.getName(), desktopFolder.getThreads(), forkJoinTask);
			ThreadUtilities.sleep(5000);
			ThreadUtilities.cancellForkJoinPool(desktop.getName());
			// Verify that there are some documents in the index
			assertNotNull("The index writer should still be available : ", desktop.getIndexWriters());
			assertEquals("There should only be one index writer : ", 1, desktop.getIndexWriters().length);
		} finally {
			desktop.setThrottle(0);
			IndexManager.closeIndexWriters(desktop);
			if (directory != null) {
				directory.close();
			}
		}
	}

}