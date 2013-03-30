package ikube.index.handler.strategy;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;
import ikube.Integration;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.filesystem.IndexableFilesystemCsvHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Document me.
 * 
 * @author Michael Couck
 * @since 20.01.2012
 * @version 01.00
 */
public class GeospatialEnrichmentStrategyIntegration extends Integration {

	private IndexContext<?> indexContext;
	private IndexableFileSystemCsv indexableFileSystemCsv;
	private IndexableFilesystemCsvHandler indexableHandlerFilesystemCsvHandler;

	@Before
	public void before() {
		indexContext = ApplicationContextManager.getBean("min-eco");
		indexableFileSystemCsv = ApplicationContextManager.getBean("min-eco-csv-files");
		indexableHandlerFilesystemCsvHandler = ApplicationContextManager.getBean(IndexableFilesystemCsvHandler.class);
	}

	@Test
	public void aroundProcess() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "min-eco.csv");
		indexableFileSystemCsv.setPath(file.getParentFile().getAbsolutePath());
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), UriUtilities.getIp());
		indexContext.setIndexWriters(indexWriter);
		List<Future<?>> futures = indexableHandlerFilesystemCsvHandler.handleIndexable(indexContext, indexableFileSystemCsv);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		IndexManager.closeIndexWriter(indexWriter);
		String indexPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexPath);
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(latestIndexDirectory, UriUtilities.getIp())));
		printIndex(indexReader);
		for (int i = 0; i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			assertNotNull(document.get(IConstants.LAT));
			assertNotNull(document.get(IConstants.LNG));
		}
	}

}