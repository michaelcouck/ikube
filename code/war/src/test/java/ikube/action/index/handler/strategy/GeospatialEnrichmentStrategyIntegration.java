package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.filesystem.IndexableFilesystemCsvHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.01.2012
 */
public class GeospatialEnrichmentStrategyIntegration extends IntegrationTest {

    private IndexContext<?> indexContext;
    private IndexableFileSystemCsv indexableFileSystemCsv;
    private IndexableFilesystemCsvHandler indexableHandlerFilesystemCsvHandler;

    @Before
    public void before() {
        indexContext = ApplicationContextManager.getBean("geoname-csv");
        indexableFileSystemCsv = ApplicationContextManager.getBean("geoname-csv-files");
        indexableHandlerFilesystemCsvHandler = ApplicationContextManager.getBean(IndexableFilesystemCsvHandler.class);
    }

    @Test
    public void aroundProcess() throws Exception {
        File file = FileUtilities.findFileRecursively(new File("."), "min-eco.csv");
        indexableFileSystemCsv.setPath(file.getParentFile().getAbsolutePath());
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), UriUtilities.getIp());
        indexContext.setIndexWriters(indexWriter);
        ForkJoinTask<?> forkJoinTask = indexableHandlerFilesystemCsvHandler.handleIndexableForked(indexContext, indexableFileSystemCsv);

        ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableFileSystemCsv.getThreads(), forkJoinTask);
        ThreadUtilities.sleep(15000);
        ThreadUtilities.cancelForkJoinPool(indexContext.getName());

        IndexManager.closeIndexWriter(indexWriter);
        String indexPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexPath);
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(latestIndexDirectory, UriUtilities.getIp())));
        printIndex(indexReader);
        for (int i = 0; i < indexReader.numDocs(); i++) {
            Document document = indexReader.document(i);
            assertNotNull(document.get(IConstants.LAT));
            assertNotNull(document.get(IConstants.LNG));
        }
    }

}