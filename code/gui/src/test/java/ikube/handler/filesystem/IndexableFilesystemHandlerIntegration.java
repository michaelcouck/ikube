package ikube.handler.filesystem;

import static org.junit.Assert.assertEquals;
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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
		FileUtilities.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			File dataIndexFolder = FileUtilities.findFileRecursively(new File("."), "data");
			String dataIndexFolderPath = FileUtilities.cleanFilePath(dataIndexFolder.getAbsolutePath());
			desktopFolder.setPath(dataIndexFolderPath);
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), ip);
			List<Future<?>> threads = indexableFilesystemHandler.handle(desktop, desktopFolder);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
			IndexManager.closeIndexWriter(desktop);

			logger.info("Data folder : " + dataIndexFolder.getAbsolutePath());
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(desktop.getIndexDirectoryPath());
			logger.info("Latest index directory : " + latestIndexDirectory.getAbsolutePath());
			File indexDirectory = new File(latestIndexDirectory, ip);
			logger.info("Index directory : " + indexDirectory);
			directory = FSDirectory.open(indexDirectory);
			boolean indexExists = IndexReader.indexExists(directory);
			assertTrue("The index should be created : ", indexExists);
			// Verify that there are some documents in the index
			IndexReader indexReader = IndexReader.open(directory);
			printIndex(indexReader);
		} finally {
			if (directory != null) {
				directory.close();
			}
		}
	}

	@Test
	public void handleSingleFile() throws Exception {
		ThreadUtilities.initialize();
		try {
			File file = FileUtilities.findFileRecursively(new File("."), "txt");
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), ip);
			desktop.setIndexWriter(indexWriter);
			desktopFolder.setPath(file.getAbsolutePath());
			List<Future<?>> futures = indexableFilesystemHandler.handle(desktop, desktopFolder);
			ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
			assertEquals("There should be one document in the index : ", 1, desktop.getIndexWriter().numDocs());
		} finally {
			ThreadUtilities.destroy();
		}
	}

	private static void printIndex(final IndexReader indexReader) throws Exception {
		for (int i = 0; i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			System.out.println("Document : " + i);
			List<Fieldable> fields = document.getFields();
			for (Fieldable fieldable : fields) {
				String fieldName = fieldable.name();
				String fieldValue = fieldable.stringValue();
				int fieldLength = fieldValue != null ? fieldValue.length() : 0;
				System.out.println("        : " + fieldName + ", " + fieldLength);
			}
		}
	}

}
