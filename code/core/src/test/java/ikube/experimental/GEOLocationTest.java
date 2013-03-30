package ikube.experimental;

import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.toolkit.Logging;
import ikube.toolkit.PerformanceTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.tier.DistanceFieldComparatorSource;
import org.apache.lucene.spatial.tier.DistanceQueryBuilder;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

public class GEOLocationTest {

	static {
		Logging.configure();
	}

	@Test
	public void testSpatialSearch() throws Exception {
		final Directory dir = new RAMDirectory();
		final IndexWriter writer = new IndexWriter(dir, new WhitespaceAnalyzer(), MaxFieldLength.UNLIMITED);
		Examples.createExampleLocations(writer);
		writer.commit();
		writer.close(true);

		// test data
		final IndexSearcher searcher = new IndexSearcher(dir, true);
		final double testDistance = SpatialHelper.getMiles(10.0D);

		final List<String> locations = find(searcher, Examples.SEEBACH_8052, testDistance);
		for (final String location : locations) {
			System.out.println("location found: " + location);
		}
	}

	private List<String> find(final IndexSearcher searcher, final Coordinate start, final double miles) throws Exception {

		final List<String> result = new ArrayList<String>();
		final DistanceQueryBuilder dq = new DistanceQueryBuilder(start.getLat(), start.getLon(), miles, SpatialHelper.LAT_FIELD,
				SpatialHelper.LON_FIELD, CartesianTierPlotter.DEFALT_FIELD_PREFIX, true, 1, 10);

		// Create a distance sort
		// As the radius filter has performed the distance calculations
		// already, pass in the filter to reuse the results.
		final DistanceFieldComparatorSource dsort = new DistanceFieldComparatorSource(dq.getDistanceFilter());
		final Sort sort = new Sort(new SortField("geo_distance", dsort));

		final Query query = new MatchAllDocsQuery();

		// find with distance sort
		final int maxDocs = 10;
		final TopDocs hits = searcher.search(query, dq.getFilter(), maxDocs, sort);
		final Map<Integer, Double> distances = dq.getDistanceFilter().getDistances();

		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				searcher.search(query, dq.getFilter(), maxDocs, sort);
			}
		}, "Spacial search : ", 100);

		// find normal, gets unordered result
		// final TopDocs hits = searcher.search(dq.getQuery(query), maxDocs);

		for (int i = 0; i < maxDocs && i < hits.totalHits && i < hits.scoreDocs.length; i++) {
			final int docID = hits.scoreDocs[i].doc;

			final Document doc = searcher.doc(docID);

			final StringBuilder builder = new StringBuilder();
			builder.append("Ort: ").append(doc.get("name")).append(" distance: ").append(SpatialHelper.getKm(distances.get(docID)));

			result.add(builder.toString());
		}

		return result;
	}
}
