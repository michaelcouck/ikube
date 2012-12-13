package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.Integration;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.handler.filesystem.IndexableFilesystemWikiHandler;
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

public class IndexableFilesystemWikiHandlerIntegration extends Integration {

	private IndexContext<?> wikiHistoryArabic;
	private IndexableFileSystemWiki wikiHistoryDataArabic;
	private IndexableFilesystemWikiHandler indexableFilesystemHandler;

	@Before
	public void before() {
		wikiHistoryArabic = ApplicationContextManager.getBean("wikiHistoryArabic");
		wikiHistoryDataArabic = ApplicationContextManager.getBean("wikiHistoryDataArabic");
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemWikiHandler.class);
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		FileUtilities.deleteFile(new File(wikiHistoryArabic.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			new ThreadUtilities().initialize();
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(wikiHistoryArabic, System.currentTimeMillis(), ip);
			wikiHistoryArabic.setIndexWriter(indexWriter);
			indexableFilesystemHandler.handle(wikiHistoryArabic, wikiHistoryDataArabic);

			Thread.sleep(10000);

			new ThreadUtilities().destroy();
			IndexManager.closeIndexWriter(wikiHistoryArabic);

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