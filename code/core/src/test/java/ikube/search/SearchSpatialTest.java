package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.Close;
import ikube.action.Open;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.strategy.GeospatialEnrichmentStrategy;
import ikube.mock.SpellingCheckerMock;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mockit.Mockit;

import org.apache.lucene.document.Document;
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
public class SearchSpatialTest extends AbstractTest {

	private String searchString = " churches and cathedrals";
	private Coordinate zurichCoordinate = new Coordinate(47.3690239, 8.5380326, "Zürich in 8000" + searchString);
	private Coordinate schwammeningenCoordinate = new Coordinate(47.4008593, 8.5781373, "Schwammedingen" + searchString);
	private Coordinate seebackCoordinate = new Coordinate(47.4232860, 8.5422655, "Seebach" + searchString);
	private Coordinate adliswilCoordinate = new Coordinate(47.3119892, 8.5256064, "Adliswil" + searchString);

	private Coordinate[] coordinates = new Coordinate[] { zurichCoordinate, new Coordinate(47.0819237, 8.3415740, "Ebikon" + searchString), seebackCoordinate,
			schwammeningenCoordinate, adliswilCoordinate, new Coordinate(47.2237640, 8.4611790, "Knonau" + searchString),
			new Coordinate(47.1934110, 8.5230670, "Baar" + searchString) };

	private File indexDirectory;
	private Directory directory;
	private Searcher searcher;
	private IndexReader indexReader;

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(SpellingCheckerMock.class);
		// Create and index with the spatial data
		indexDirectory = new File(indexContext.getIndexDirectoryPath());
		boolean deleted = FileUtilities.deleteFile(indexDirectory, 1);
		logger.info("Deleted : " + deleted + ", index directory : " + indexDirectory);
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.TRUE);

		GeospatialEnrichmentStrategy enrichmentStrategy = new GeospatialEnrichmentStrategy();
		enrichmentStrategy.initialize();

		for (Coordinate coordinate : coordinates) {
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, coordinate.toString(), indexableColumn, document);
			enrichmentStrategy.addSpatialLocationFields(coordinate, document);
			indexWriter.addDocument(document);
		}
		IndexManager.closeIndexWriter(indexWriter);

		directory = FSDirectory.open(indexDirectory);
		indexReader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		searcher = new MultiSearcher(new Searchable[] { indexSearcher });
		// printIndex(indexReader, Integer.MAX_VALUE);
	}

	@After
	public void after() throws Exception {
		searcher.close();
		Mockit.tearDownMocks();
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
		assertNotNull(results);
		assertEquals(5, results.size());
		// Four co-ordinates fall into the search region, in this order
		assertTrue(results.get(0).get(IConstants.CONTENTS).contains(zurichCoordinate.getName()));
		assertTrue(results.get(1).get(IConstants.CONTENTS).contains(schwammeningenCoordinate.getName()));
		assertTrue(results.get(2).get(IConstants.CONTENTS).contains(seebackCoordinate.getName()));
		assertTrue(results.get(3).get(IConstants.CONTENTS).contains(adliswilCoordinate.getName()));

		searchSpatial.setSearchString("hullaballou");
		results = searchSpatial.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
	}

	@Test
	@Ignore
	public void adHoc() throws Exception {
		try {
			Open open = new Open();
			IndexContext<?> indexContext = new IndexContext<>();
			indexContext.setIndexName("geospatial");
			indexContext.setIndexDirectoryPath("/home/indexes");

			open.execute(indexContext);

			SearchSpatial searchSpatial = new SearchSpatial(indexContext.getMultiSearcher());
			searchSpatial.setDistance(10);
			searchSpatial.setFirstResult(0);
			searchSpatial.setMaxResults(10);
			searchSpatial.setFragment(Boolean.TRUE);
			searchSpatial.setSearchField("name");
			searchSpatial.setSearchString("cape town");
			searchSpatial.setCoordinate(new Coordinate(-33.9693580, 18.4622110));

			ArrayList<HashMap<String, String>> results = searchSpatial.execute();
			logger.info("Results : " + results);
			printResults(results);
			assertTrue(results.size() > 0);
		} finally {
			Close close = new Close();
			close.execute(indexContext);
		}
	}

	private void printResults(final ArrayList<HashMap<String, String>> results) {
		for (final HashMap<String, String> result : results) {
			logger.info("Result : ");
			for (final Map.Entry<String, String> mapEntry : result.entrySet()) {
				logger.info("       : " + mapEntry.getKey() + "-" + mapEntry.getValue());
			}
		}
	}

}