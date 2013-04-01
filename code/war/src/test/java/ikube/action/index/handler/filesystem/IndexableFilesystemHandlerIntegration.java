package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.Integration;
import ikube.action.index.IndexManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemHandlerIntegration extends Integration {

	private IndexContext<?> dropbox;
	private IndexableFileSystem dropboxIndexable;
	private IndexableFilesystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		dropbox = ApplicationContextManager.getBean("dropboxIndex");
		dropboxIndexable = ApplicationContextManager.getBean("dropboxIndexable");
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class.getName());
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		FileUtilities.deleteFile(new File(dropbox.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handleAndInterrupt() throws Exception {
		Directory directory = null;
		try {
			File dataIndexFolder = FileUtilities.findFileRecursively(new File("."), "data");
			String dataIndexFolderPath = FileUtilities.cleanFilePath(dataIndexFolder.getAbsolutePath());
			dropboxIndexable.setPath(dataIndexFolderPath);
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(dropbox, System.currentTimeMillis(), ip);
			dropbox.setIndexWriters(indexWriter);
			dropbox.setThrottle(60000);

			ThreadUtilities.submit("interrupt-test", new Runnable() {
				public void run() {
					ThreadUtilities.sleep(15000);
					ThreadUtilities.destroy(dropbox.getIndexName());
				}
			});

			List<Future<?>> futures = indexableFilesystemHandler.handleIndexable(dropbox, dropboxIndexable);
			ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);

			boolean atLeastOneCancelled = Boolean.FALSE;
			for (final Future<?> future : futures) {
				logger.info("Future : " + future);
				atLeastOneCancelled |= future.isCancelled();
			}
			dropbox.setThrottle(0);
			assertTrue(atLeastOneCancelled);

			// Verify that there are some documents in the index
			assertTrue("There should be at least one document in the index : ", dropbox.getIndexWriters()[0].numDocs() > 0);
		} finally {
			IndexManager.closeIndexWriters(dropbox);
			if (directory != null) {
				directory.close();
			}
		}
	}

}