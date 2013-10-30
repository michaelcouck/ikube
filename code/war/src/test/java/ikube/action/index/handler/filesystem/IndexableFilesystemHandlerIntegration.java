package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemHandlerIntegration extends IntegrationTest {

	private IndexWriter indexWriter;
	private IndexContext<?> desktop;
	private IndexableFileSystem desktopFolder;
	private IndexableFileSystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		String dataIndexFolderPath = FileUtilities.cleanFilePath(new File(".").getAbsolutePath());
		desktop = ApplicationContextManager.getBean("desktop");
		desktopFolder = ApplicationContextManager.getBean("desktopFolder");
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFileSystemHandler.class.getName());
		indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), UriUtilities.getIp());

		desktopFolder.setPath(dataIndexFolderPath);
		desktopFolder.setExcludedPattern(null);
		// This should be true for performance testing, however there is a problem with running this test
		// in Eclipse with the unpack to true, OpenJpa throws a stack over flow for some reason, I think because
		// the classes are not enhanced
		desktopFolder.setUnpackZips(Boolean.FALSE);
		desktop.setIndexWriters(indexWriter);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handleIndexable() throws Exception {
		try {
			ForkJoinTask<?> forkJoinTask = indexableFilesystemHandler.handleIndexableForked(desktop, desktopFolder);
			ThreadUtilities.executeForkJoinTasks(desktop.getName(), desktopFolder.getThreads(), forkJoinTask);
			ThreadUtilities.sleep(15000);
			ThreadUtilities.cancellForkJoinPool(desktop.getName());
			// Verify that there are some documents in the index
			assertNotNull("The index writer should still be available : ", desktop.getIndexWriters());
			assertEquals("There should only be one index writer : ", 1, desktop.getIndexWriters().length);
			assertTrue(indexWriter.numDocs() > 0);
		} finally {
			IndexManager.closeIndexWriters(desktop);
		}
	}

}