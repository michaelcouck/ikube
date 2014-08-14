package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.strategy.GeospatialEnrichmentStrategy;
import ikube.model.Coordinate;
import ikube.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.02.2012
 */
public class SearchSpatialTest extends AbstractTest {

	private IndexSearcher searcher;

	private Coordinate zurich = new Coordinate(47.3690239, 8.5380326, "Zürich, Switzerland");
	private Coordinate schwammeningen = new Coordinate(47.4008593, 8.5781373, "Schwammedingen, Switzerland");
	private Coordinate seeback = new Coordinate(47.4232860, 8.5422655, "Seebach, Switzerland");
	private Coordinate adliswil = new Coordinate(47.3119892, 8.5256064, "Adliswil, Switzerland");
	private Coordinate ebikon = new Coordinate(47.0819237, 8.3415740, "Ebikon, Switzerland");
	private Coordinate knonau = new Coordinate(47.2237640, 8.4611790, "Knonau, Switzerland");
	private Coordinate baar = new Coordinate(47.1934110, 8.5230670, "Baar, Switzerland");

	private Coordinate[] coordinates = new Coordinate[]{zurich, ebikon, seeback, schwammeningen, adliswil, knonau, baar};

	@Before
	public void before() throws Exception {
		Mockito.when(indexableColumn.isStored()).thenReturn(Boolean.TRUE);
		Mockito.when(indexableColumn.isAnalyzed()).thenReturn(Boolean.TRUE);
		Mockito.when(indexableColumn.isTokenized()).thenReturn(Boolean.TRUE);
		Mockito.when(indexableColumn.isVectored()).thenReturn(Boolean.FALSE);

		RAMDirectory ramDirectory = new RAMDirectory();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, ramDirectory, Boolean.TRUE);
		GeospatialEnrichmentStrategy enrichmentStrategy = new GeospatialEnrichmentStrategy();
        Deencapsulation.setField(enrichmentStrategy, "dataBase", dataBase);
		enrichmentStrategy.initialize();
		for (final Coordinate coordinate : coordinates) {
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, coordinate.getName(), indexableColumn, document);
			IndexManager.addStringField(IConstants.LATITUDE, Double.toString(coordinate.getLatitude()), indexableColumn, document);
			IndexManager.addStringField(IConstants.LONGITUDE, Double.toString(coordinate.getLongitude()), indexableColumn, document);
			enrichmentStrategy.addSpatialLocationFields(coordinate, document);
			indexWriter.addDocument(document);
		}
		indexWriter.commit();
		indexWriter.maybeMerge();
		indexWriter.forceMerge(5);
		IndexReader indexReader = DirectoryReader.open(ramDirectory);
		searcher = new IndexSearcher(indexReader);
	}

	@After
	public void after() throws Exception {
        if (searcher != null && searcher.getIndexReader() != null) {
            searcher.getIndexReader().close();
        }
	}

	@Test
	public void searchSpatial() throws Exception {
		final SearchSpatial searchSpatial = new SearchSpatial(searcher);
		searchSpatial.setDistance(10);
		searchSpatial.setFirstResult(0);
		searchSpatial.setMaxResults(10);
		searchSpatial.setFragment(Boolean.TRUE);
		searchSpatial.setSearchStrings("Switzerland");
		searchSpatial.setSearchFields(IConstants.CONTENTS);
		searchSpatial.setOccurrenceFields(IConstants.SHOULD);
		searchSpatial.setTypeFields(Search.TypeField.STRING.name());

		searchSpatial.setCoordinate(zurich);

		ArrayList<HashMap<String, String>> results = searchSpatial.execute();

		assertEquals(5, results.size());
		// Four co-ordinates fall into the search region, in this order
		assertTrue(results.get(0).get(IConstants.CONTENTS).contains(zurich.getName()));
		assertTrue(results.get(1).get(IConstants.CONTENTS).contains(schwammeningen.getName()));
		assertTrue(results.get(2).get(IConstants.CONTENTS).contains(seeback.getName()));
		assertTrue(results.get(3).get(IConstants.CONTENTS).contains(adliswil.getName()));
		verifyDistances(results);

		// Make the circle a little wider
		searchSpatial.setDistance(20);
		results = searchSpatial.execute();
		assertEquals(7, results.size());
		verifyDistances(results);

		// And still a little wider
		searchSpatial.setDistance(50);
		results = searchSpatial.execute();
		assertEquals(8, results.size());
		verifyDistances(results);

		// Reduce the results to no hits
		searchSpatial.setSearchStrings("hullaballou");
		results = searchSpatial.execute();
		assertEquals(1, results.size());

		searchSpatial.setSearchStrings("Switzerland");
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				searchSpatial.execute();
			}
		}, "Spatial search performance : ", 1000, Boolean.TRUE);
		assertTrue(perSecond > 100);
	}

    @Test
    public void search() throws IOException {
        Directory directory = FSDirectory.open(new File("/tmp/indexes/geospatial/1407942554961/192.168.1.8-8000"));
        IndexReader indexReader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(indexReader);

        SearchSpatial searchSpatial = new SearchSpatial(searcher);
        searchSpatial.setDistance(1000);
        searchSpatial.setFirstResult(0);
        searchSpatial.setMaxResults(10);
        searchSpatial.setFragment(Boolean.TRUE);
        searchSpatial.setSearchStrings("hotel");
        searchSpatial.setSearchFields(IConstants.NAME);
        searchSpatial.setOccurrenceFields(IConstants.SHOULD);
        searchSpatial.setTypeFields(Search.TypeField.STRING.name());

        searchSpatial.setCoordinate(new Coordinate(-33.9, 18.4));


        ArrayList<HashMap<String, String>> results = searchSpatial.execute();
        System.out.println(results.size() + ", " + results.get(results.size() - 1));
        assertTrue(results.size() > 3);
    }

	private void verifyDistances(final ArrayList<HashMap<String, String>> results) {
		double previousDistance = 0;
		for (final HashMap<String, String> result : results) {
			String string = result.get(IConstants.DISTANCE);
			if (string == null) {
				continue;
			}
			double distance = Double.parseDouble(string);
			logger.info("Prev : " + previousDistance + ", dist : " + distance);
			assertTrue(distance >= previousDistance);
			previousDistance = distance;
		}
	}

}