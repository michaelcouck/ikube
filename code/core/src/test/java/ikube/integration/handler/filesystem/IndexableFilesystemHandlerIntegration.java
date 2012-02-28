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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
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
		dropboxIndexable = ApplicationContextManager.getBean("filesystemIndexable");
		dropboxIndexContext = ApplicationContextManager.getBean("filesystemIndex");
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
			IndexManager.closeIndexWriter(dropboxIndexContext);
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
			// Verify that there are some documents in the index
			IndexReader indexReader = IndexReader.open(directory);
			printIndex(indexReader);
		} finally {
			if (directory != null) {
				directory.close();
			}
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
	
	public static void main(String[] args) throws Exception {
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File("./dropBoxIndex/dropboxIndex/1329386771703/10.100.109.138")));
		printIndex(indexReader); 
	}

}
