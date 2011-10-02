package ikube.integration;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.index.spatial.Coordinate;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.search.SearchSpatial;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO Elaborate this test to actually test the index.
 * 
 * @author Michael Couck
 * @since 28.09.2011
 * @version 01.00
 */
@Ignore
public class IndexIntegration {

	private Logger	logger				= Logger.getLogger(this.getClass());
	private String	indexDirectoryPath	= "./indexes";
	private String	ikubeFolder			= "./" + IConstants.IKUBE;

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
		FileUtilities.deleteFile(new File(indexDirectoryPath), 1);
		// File externalFolder = FileUtilities.findFileRecursively(new File("."), "META-INF");
		// FileUtilities.copyFiles(externalFolder, new File(ikubeFolder), "svn", "spelling", "flatfile", "persistence");
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
		FileUtilities.deleteFile(new File(indexDirectoryPath), 1);
	}

	@Test
	public void index() throws Exception {
		ApplicationContextManager.getApplicationContext();
		Thread.sleep(10000);
		IndexableTableHandler indexableTableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);
		IndexContext<?> indexContext = ApplicationContextManager.getBean("patientIndex");
		IndexableTable indexableTable = ApplicationContextManager.getBean("patientTable");
		IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), "127.0.0.1");
		List<Thread> threads = indexableTableHandler.handle(indexContext, indexableTable);
		ThreadUtilities.waitForThreads(threads);
		IndexManager.closeIndexWriter(indexContext);
		searchGeoSpatial();
	}

	@Test
	public void searchGeoSpatial() throws Exception {
		Directory directory = null;
		IndexSearcher indexSearcher = null;
		try {
			File indexDirectory = FileUtilities.findFileRecursively(new File("."), "127.0.0.1");

			directory = FSDirectory.open(indexDirectory);

			IndexReader indexReader = IndexReader.open(directory);
			for (int i = 0; i < 10; i++) {
				Document document = indexReader.document(i);
				logger.info("Document : " + document);
			}

			indexSearcher = new IndexSearcher(indexReader);
			SearchSpatial searchSpatial = new SearchSpatial(indexSearcher);
			searchSpatial.setDistance(1);
			searchSpatial.setFirstResult(0);
			searchSpatial.setMaxResults(10);
			searchSpatial.setFragment(Boolean.TRUE);
			searchSpatial.setSearchField("firstName");
			searchSpatial.setSearchString("deprecatory");

			double antwerpLatitude = 51.216667;
			double antwerpLongitude = 4.416667;

			Coordinate antwerp = new Coordinate(antwerpLatitude, antwerpLongitude);
			searchSpatial.setCoordinate(antwerp);

			List<Map<String, String>> results = searchSpatial.execute();
			for (Map<String, String> result : results) {
				for (Map.Entry<String, String> entry : result.entrySet()) {
					logger.info("Key : " + entry.getKey() + ", " + entry.getValue());
				}
				logger.info("");
			}
			assertNotNull(results);
		} finally {
			if (indexSearcher != null) {
				try {
					indexSearcher.close();
				} catch (Exception e) {
					logger.error("Exception closing the index : ", e);
				}
			}
		}
	}

	@Test
	public void printIndex() throws Exception {
		String indexDirectoryName = "127.0.0.1";
		File indexDirectory = FileUtilities.findFileRecursively(new File("."), indexDirectoryName);
		Directory directory = FSDirectory.open(indexDirectory);
		IndexReader indexReader = IndexReader.open(directory);
		for (int i = 0; i < indexReader.numDocs() && i < 10; i++) {
			Document document = indexReader.document(i);
			logger.error(document);
			List<Fieldable> fields = document.getFields();
			for (Fieldable fieldable : fields) {
				logger.error(fieldable);
			}
		}
		// FileUtilities.deleteFile(new File("./indexes"), 1);
	}

}
