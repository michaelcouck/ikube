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
import ikube.mock.SpellingCheckerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.02.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchSpatialTest extends ATest {

	private String searchString = " churches and cathedrals";
	private Coordinate zurichCoordinate = new Coordinate(47.3690239, 8.5380326, "Zürich in 8000" + searchString);
	private Coordinate schwammeningenCoordinate = new Coordinate(47.4008593, 8.5781373, "Schwammedingen" + searchString);
	private Coordinate seebackCoordinate = new Coordinate(47.4232860, 8.5422655, "Seebach" + searchString);
	private Coordinate adliswilCoordinate = new Coordinate(47.3119892, 8.5256064, "Adliswil" + searchString);

	private Coordinate[] coordinates = new Coordinate[] { zurichCoordinate, new Coordinate(47.0819237, 8.3415740, "Ebikon" + searchString),
			seebackCoordinate, schwammeningenCoordinate, adliswilCoordinate,
			new Coordinate(47.2237640, 8.4611790, "Knonau" + searchString), new Coordinate(47.1934110, 8.5230670, "Baar" + searchString) };

	private File indexDirectory;
	private Directory directory;
	private Searcher searcher;
	private IndexReader indexReader;

	public SearchSpatialTest() {
		super(SearchSpatialTest.class);
	}

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(SpellingCheckerMock.class);
		// Create and index with the spatial data
		indexDirectory = new File(indexContext.getIndexDirectoryPath());
		boolean deleted = FileUtilities.deleteFile(indexDirectory, 1);
		logger.info("Deleted : " + deleted + ", index directory : " + indexDirectory);
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.TRUE);
		IEnrichment enrichment = new Enrichment();
		enrichment.setMinKm(10);
		enrichment.setMaxKm(20);
		for (Coordinate coordinate : coordinates) {
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, coordinate.toString(), document, Store.YES, Index.ANALYZED, TermVector.YES);
			enrichment.addSpatialLocationFields(coordinate, document);
			indexWriter.addDocument(document);
		}
		IndexManager.closeIndexWriter(indexWriter);

		directory = FSDirectory.open(indexDirectory);
		indexReader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		searcher = new MultiSearcher(new Searchable[] { indexSearcher });
	}

	@After
	public void after() throws Exception {
		searcher.close();
		FileUtilities.deleteFile(indexDirectory, 1);
	}

	@Test
	public void searchSpatial() throws Exception {
		SearchSpatial searchSpatial = new SearchSpatial(searcher);
		searchSpatial.setDistance(10);
		searchSpatial.setFirstResult(0);
		searchSpatial.setMaxResults(10);
		searchSpatial.setFragment(Boolean.TRUE);
		searchSpatial.setSearchField(IConstants.CONTENTS);
		searchSpatial.setSearchString(zurichCoordinate.getName());
		searchSpatial.setCoordinate(zurichCoordinate);
		ArrayList<HashMap<String, String>> results = searchSpatial.execute();
		logger.info("Results : " + results);
		assertNotNull(results);
		assertEquals(5, results.size());
		// Four co-ordinates fall into the search region, in this order
		assertTrue(results.get(0).get(IConstants.CONTENTS).equals(zurichCoordinate.toString()));
		assertTrue(results.get(1).get(IConstants.CONTENTS).equals(schwammeningenCoordinate.toString()));
		assertTrue(results.get(2).get(IConstants.CONTENTS).equals(seebackCoordinate.toString()));
		assertTrue(results.get(3).get(IConstants.CONTENTS).equals(adliswilCoordinate.toString()));

		searchSpatial = new SearchSpatialAll(searcher);
		searchSpatial.setDistance(10);
		searchSpatial.setFirstResult(0);
		searchSpatial.setMaxResults(10);
		searchSpatial.setFragment(Boolean.TRUE);
		searchSpatial.setSearchString(zurichCoordinate.getName());
		searchSpatial.setCoordinate(zurichCoordinate);
		results = searchSpatial.execute();
		logger.info("Results : " + results);
		assertNotNull(results);
		assertEquals(5, results.size());
		// Four co-ordinates fall into the search region, in this order
		assertTrue(results.get(0).get(IConstants.CONTENTS).equals(zurichCoordinate.toString()));
		assertTrue(results.get(1).get(IConstants.CONTENTS).equals(schwammeningenCoordinate.toString()));
		assertTrue(results.get(2).get(IConstants.CONTENTS).equals(seebackCoordinate.toString()));
		assertTrue(results.get(3).get(IConstants.CONTENTS).equals(adliswilCoordinate.toString()));
	}

	@Test
	@Ignore
	public void searchGeospatialIndex() throws Exception {
		MultiSearcher indexSearcher = getIndexSearcher();
		try {
			SearchSpatialAll searchSpatial = new SearchSpatialAll(indexSearcher);
			searchGeospatialIndex(searchSpatial, 10); // Minimum results within 10 km
		} finally {
			if (indexSearcher != null) {
				indexSearcher.close();
			}
		}
	}

	private MultiSearcher getIndexSearcher() throws Exception {
		Directory directory = FSDirectory.open(new File("/usr/share/eclipse/workspace/ikube/code/war/geospatial"));
		IndexReader indexReader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		return new MultiSearcher(indexSearcher);
	}

	private void searchGeospatialIndex(SearchSpatial searchSpatial, int distance) throws Exception {
		Coordinate coordinate = new Coordinate(52.52274, 13.4166);
		searchSpatial.setCoordinate(coordinate);
		searchSpatial.setDistance(distance);
		searchSpatial.setFirstResult(0);
		searchSpatial.setFragment(true);
		searchSpatial.setMaxResults(1);
		String searchString = "berlin";
		searchSpatial.setSearchString(searchString); // searchString, searchString, searchString
		ArrayList<HashMap<String, String>> results = searchSpatial.execute();
		logger.info("Results : " + distance + ", " + results.get(results.size() - 1));
		for (HashMap<String, String> result : results) {
			logger.info(result.toString());
		}
	}

}