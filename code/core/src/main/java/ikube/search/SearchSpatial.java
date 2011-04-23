package ikube.search;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.tier.DistanceFieldComparatorSource;
import org.apache.lucene.spatial.tier.DistanceQueryBuilder;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;

/**
 * Executes a spatial search. One of these classes needs to be instantiated for each search as the member variables are for a particular
 * search.
 * 
 * @see Search
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class SearchSpatial extends Search {

	private transient Sort sort;
	private transient Map<Integer, Double> distances;
	private transient DistanceQueryBuilder queryBuilder;

	// TODO Set these fields
	private final transient int maxDocs = 10;
	private transient Coordinate coordinate;
	private final transient int distance = 10;

	public SearchSpatial(final Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		TopDocs topDocs = searcher.search(query, queryBuilder.getFilter(), maxDocs, sort);
		distances = queryBuilder.getDistanceFilter().getDistances();
		return topDocs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Query getQuery() throws ParseException {
		queryBuilder = new DistanceQueryBuilder(coordinate.getLat(), coordinate.getLon(), distance, IConstants.LAT, IConstants.LNG,
				CartesianTierPlotter.DEFALT_FIELD_PREFIX, true);
		// Create a distance sort
		// As the radius filter has performed the distance calculations
		// already, pass in the filter to reuse the results.
		DistanceFieldComparatorSource fieldComparator = new DistanceFieldComparatorSource(queryBuilder.getDistanceFilter());
		sort = new Sort(new SortField("geo_distance", fieldComparator));
		return new MatchAllDocsQuery();
	}

	public List<Map<String, String>> execute() {
		if (searcher == null) {
			logger.warn("No searcher on any index, is an index created?");
		}
		long duration = 0;
		long totalHits = 0;
		List<Map<String, String>> results = null;
		try {
			Query query = getQuery();
			long start = System.currentTimeMillis();
			TopDocs topDocs = search(query);
			duration = System.currentTimeMillis() - start;
			totalHits = topDocs.totalHits;
			results = getResults(topDocs, query);
			for (int i = 0; i < maxDocs && i < topDocs.totalHits && i < topDocs.scoreDocs.length; i++) {
				final int docID = topDocs.scoreDocs[i].doc;
				double distanceFromOrigin = distances.get(docID);
				Map<String, String> result = results.get(i);
				result.put(IConstants.DISTANCE, Double.toString(distanceFromOrigin));
			}
		} catch (Exception e) {
			logger.error("Exception searching for string " + searchStrings[0] + " in searcher " + searcher, e);
			if (results == null) {
				results = new ArrayList<Map<String, String>>();
			}
		}
		// Add the search results size as a last result
		addStatistics(results, totalHits, duration);
		return results;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

}