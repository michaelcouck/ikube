package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.Integration;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.handler.filesystem.IndexableFilesystemHandler;
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

	private IndexContext<?> desktop;
	private IndexableFileSystem desktopFolder;
	private IndexableFilesystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		desktop = ApplicationContextManager.getBean("desktop");
		desktopFolder = ApplicationContextManager.getBean("desktopFolder");
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		// FileUtilities.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			ThreadUtilities.initialize();
			File dataIndexFolder = FileUtilities.findFileRecursively(new File("."), "data");
			String dataIndexFolderPath = FileUtilities.cleanFilePath(dataIndexFolder.getAbsolutePath());
			desktopFolder.setPath(dataIndexFolderPath);
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), ip);
			desktop.setIndexWriter(indexWriter);
			List<Future<?>> threads = indexableFilesystemHandler.handle(desktop, desktopFolder);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);

			logger.info("Data folder : " + dataIndexFolder.getAbsolutePath());
			File latestIndexDirectory = IndexManager.getLatestIndexDirectory(desktop.getIndexDirectoryPath());
			logger.info("Latest index directory : " + latestIndexDirectory.getAbsolutePath());
			File indexDirectory = new File(latestIndexDirectory, ip);
			logger.info("Index directory : " + indexDirectory);

			// Verify that there are some documents in the index
			logger.info("Num docs : " + desktop.getIndexWriter().numDocs());
			assertTrue("There should be at least one document in the index : ", desktop.getIndexWriter().numDocs() > 0);
		} finally {
			IndexManager.closeIndexWriter(desktop);
			if (directory != null) {
				directory.close();
			}
		}
	}

}
