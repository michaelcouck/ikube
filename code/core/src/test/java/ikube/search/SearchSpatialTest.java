package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.spatial.Coordinate;
import ikube.index.spatial.enrich.Enrichment;
import ikube.index.spatial.enrich.IEnrichment;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SearchSpatialTest extends ATest {

	private static final String SEARCH_STRING = " churches and cathedrals";
	private static final Coordinate ZURICH_COORDINATE = new Coordinate(47.3690239, 8.5380326, "Zürich in 8000" + SEARCH_STRING);
	private static final Coordinate SCHWAMMEDINGEN_COORDINATE = new Coordinate(47.4008593, 8.5781373, "Schwammedingen" + SEARCH_STRING);
	private static final Coordinate SEEBACH_COORDINATE = new Coordinate(47.4232860, 8.5422655, "Seebach" + SEARCH_STRING);
	private static final Coordinate ADLISWIL_COORDINATE = new Coordinate(47.3119892, 8.5256064, "Adliswil" + SEARCH_STRING);

	private static final Coordinate[] COORDINATES = new Coordinate[] { //
	ZURICH_COORDINATE, //
			new Coordinate(47.0819237, 8.3415740, "Ebikon" + SEARCH_STRING), //
			SEEBACH_COORDINATE, //
			SCHWAMMEDINGEN_COORDINATE, //
			ADLISWIL_COORDINATE, //
			new Coordinate(47.2237640, 8.4611790, "Knonau" + SEARCH_STRING), //
			new Coordinate(47.1934110, 8.5230670, "Baar" + SEARCH_STRING) //
	};

	private File indexDirectory = new File(INDEX_CONTEXT.getIndexDirectoryPath());

	public SearchSpatialTest() {
		super(SearchSpatialTest.class);
	}

	@Before
	public void before() throws Exception {
		// Create and index with the spatial data
		boolean deleted = FileUtilities.deleteFile(indexDirectory, 1);
		logger.info("Deleted : " + deleted + ", index directory : " + indexDirectory);
		IndexWriter indexWriter = IndexManager.openIndexWriter(INDEX_CONTEXT, indexDirectory, Boolean.TRUE);
		IEnrichment enrichment = new Enrichment();
		enrichment.setMinKm(10);
		enrichment.setMaxKm(20);
		for (Coordinate coordinate : COORDINATES) {
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, coordinate.toString(), document, Store.YES, Index.ANALYZED, TermVector.YES);
			enrichment.addSpatialLocationFields(coordinate, document);
			indexWriter.addDocument(document);
		}
		IndexManager.closeIndexWriter(indexWriter);
	}

	@Test
	public void searchSpatial() throws Exception {
		Directory directory = null;
		IndexSearcher indexSearcher = null;
		try {
			directory = FSDirectory.open(indexDirectory);

			IndexReader indexReader = IndexReader.open(directory);
			for (int i = 0; i < 3; i++) {
				Document document = indexReader.document(i);
				logger.info("Document : " + document);
			}
			
			indexSearcher = new IndexSearcher(indexReader);
			SearchSpatial searchSpatial = new SearchSpatial(indexSearcher);
			searchSpatial.setFirstResult(0);
			searchSpatial.setFragment(Boolean.TRUE);
			searchSpatial.setMaxResults(100);
			searchSpatial.setSearchField(IConstants.CONTENTS);
			searchSpatial.setSearchString(ZURICH_COORDINATE.getName());
			searchSpatial.setSortField(IConstants.CONTENTS);
			searchSpatial.setCoordinate(ZURICH_COORDINATE);
			searchSpatial.setDistance(10);
			searchSpatial.setMaxResults(100);
			List<Map<String, String>> results = searchSpatial.execute();
			logger.info("Results : " + results);
			assertNotNull(results);
			assertEquals(5, results.size());
			// Four co-ordinates fall into the search region, in this order
			assertTrue(results.get(0).get(IConstants.CONTENTS).equals(ZURICH_COORDINATE.toString()));
			assertTrue(results.get(1).get(IConstants.CONTENTS).equals(SCHWAMMEDINGEN_COORDINATE.toString()));
			assertTrue(results.get(2).get(IConstants.CONTENTS).equals(SEEBACH_COORDINATE.toString()));
			assertTrue(results.get(3).get(IConstants.CONTENTS).equals(ADLISWIL_COORDINATE.toString()));
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
	// @Ignore
	public void searchGeoSpatial() throws Exception {
		Directory directory = null;
		IndexSearcher indexSearcher = null;
		try {
			String indexPath = "D:/cluster/indexes/geospatial/1305662411676/192.168.56.1.10837512398503";

			directory = FSDirectory.open(new File(indexPath));

			IndexReader indexReader = IndexReader.open(directory);
			for (int i = 0; i < 10; i++) {
				Document document = indexReader.document(i);
				logger.info("Document : " + document);
			}

			indexSearcher = new IndexSearcher(indexReader);
			SearchSpatial searchSpatial = new SearchSpatial(indexSearcher);
			searchSpatial.setFirstResult(0);
			searchSpatial.setFragment(Boolean.TRUE);
			searchSpatial.setMaxResults(10);
			searchSpatial.setSearchField(IConstants.FEATURECLASS, IConstants.FEATURECODE, IConstants.COUNTRYCODE);
			searchSpatial.setSearchString("A", "PCLI", "ZA");

			searchSpatial.setSortField(IConstants.CONTENTS);
			searchSpatial.setCoordinate(new Coordinate(-30.0, 26.0));

			searchSpatial.setDistance(10000);

			List<Map<String, String>> results = searchSpatial.execute();
			logger.info("Results : " + results);
			assertNotNull(results);
			assertEquals(2, results.size());
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

	@After
	public void after() throws Exception {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

}
