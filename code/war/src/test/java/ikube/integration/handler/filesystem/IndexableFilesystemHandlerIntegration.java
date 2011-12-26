package ikube.integration.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.handler.filesystem.IndexableFilesystemHandler;
import ikube.integration.AbstractIntegration;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemHandlerIntegration extends AbstractIntegration {

	private IndexableFileSystem dropboxIndexable;
	private IndexContext<?> dropboxIndexContext;
	private IndexableFilesystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		dropboxIndexable = ApplicationContextManager.getBean("wikidata");
		dropboxIndexContext = ApplicationContextManager.getBean("indexContext");
		dropboxIndexContext.setAction(new Action());
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		FileUtilities.deleteFile(new File(dropboxIndexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexManager.openIndexWriter(dropboxIndexContext, System.currentTimeMillis(), ip);
			List<Future<?>> threads = indexableFilesystemHandler.handle(dropboxIndexContext, dropboxIndexable);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
			File dropboxIndexFolder = FileUtilities.findFileRecursively(new File(dropboxIndexContext.getIndexDirectoryPath()),
					"dropboxIndex");
			logger.info("Dropbox folder : " + dropboxIndexFolder.getAbsolutePath());
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(dropboxIndexFolder.getAbsolutePath());
			logger.info("Latest index directory : " + latestIndexDirectory.getAbsolutePath());
			File indexDirectory = new File(latestIndexDirectory, ip);
			logger.info("Index directory : " + indexDirectory);
			directory = FSDirectory.open(indexDirectory);
			boolean indexExists = IndexReader.indexExists(directory);
			assertTrue("The index should be created : ", indexExists);
		} finally {
			if (directory != null) {
				directory.close();
			}
		}
	}

}
