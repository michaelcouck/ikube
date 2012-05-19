package ikube.integration.handler.filesystem;

import static org.junit.Assert.assertEquals;
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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemHandlerIntegration extends AbstractIntegration {

	private IndexableFileSystem localFilesystemIndexable;
	private IndexContext<?> localFilesystemIndexContext;
	private IndexableFilesystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		localFilesystemIndexable = ApplicationContextManager.getBean("localFileSystemIndexable");
		localFilesystemIndexContext = ApplicationContextManager.getBean("localFileSystemContext");
		localFilesystemIndexContext.setAction(new Action());
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		FileUtilities.deleteFile(new File(localFilesystemIndexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			File dataIndexFolder = FileUtilities.findFileRecursively(new File("."), "data");
			localFilesystemIndexable.setPath(dataIndexFolder.getAbsolutePath());
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexManager.openIndexWriter(localFilesystemIndexContext, System.currentTimeMillis(), ip);
			List<Future<?>> threads = indexableFilesystemHandler.handle(localFilesystemIndexContext, localFilesystemIndexable);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
			IndexManager.closeIndexWriter(localFilesystemIndexContext);

			logger.info("Data folder : " + dataIndexFolder.getAbsolutePath());
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(localFilesystemIndexContext.getIndexDirectoryPath());
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
		File file = FileUtilities.findFileRecursively(new File("."), "txt");
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(localFilesystemIndexContext, System.currentTimeMillis(), ip);
		localFilesystemIndexContext.getIndex().setIndexWriter(indexWriter);
		localFilesystemIndexable.setPath(file.getAbsolutePath());
		List<Future<?>> futures = indexableFilesystemHandler.handle(localFilesystemIndexContext, localFilesystemIndexable);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		assertEquals("There should be one document in the index : ", 1, localFilesystemIndexContext.getIndex().getIndexWriter().numDocs());
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
