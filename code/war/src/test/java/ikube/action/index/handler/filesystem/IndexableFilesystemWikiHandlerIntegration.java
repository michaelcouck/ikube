package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.filesystem.IndexableFilesystemWikiHandler;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemWiki;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.net.InetAddress;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemWikiHandlerIntegration extends IntegrationTest {

	private IndexContext<?> wikiHistoryArabic;
	private IndexableFileSystemWiki wikiHistoryDataArabic;
	private IndexableFilesystemWikiHandler indexableFilesystemHandler;

	@Before
	public void before() {
		wikiHistoryArabic = ApplicationContextManager.getBean("wikiHistoryArabic");
		wikiHistoryDataArabic = ApplicationContextManager.getBean("wikiHistoryDataArabic");
		File file = FileUtilities.findFileRecursively(new File("."), "arwiki-latest-pages-meta-history.xml.bz2.100.gig.bz2");
		wikiHistoryDataArabic.setPath(file.getParentFile().getAbsolutePath());
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemWikiHandler.class);
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		FileUtilities.deleteFile(new File(wikiHistoryArabic.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(wikiHistoryArabic, System.currentTimeMillis(), ip);
			wikiHistoryArabic.setIndexWriters(indexWriter);
			indexableFilesystemHandler.handleIndexable(wikiHistoryArabic, wikiHistoryDataArabic);

			Thread.sleep(10000);

			ThreadUtilities.destroy(wikiHistoryArabic.getIndexName());
			IndexManager.closeIndexWriters(wikiHistoryArabic);

			File latestIndexDirectory = IndexManager.getLatestIndexDirectory(wikiHistoryArabic.getIndexDirectoryPath());
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