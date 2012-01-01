package ikube.search;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class SearchSpatial extends SearchMulti {

	/** The distance from the origin that we will accept in the results. */
	private transient int distance;
	/** The origin, i.e. the starting point for the distance search. */
	private transient Coordinate coordinate;

	/** The distances from the point of origin, i.e. the input coordinate. */
	private transient Map<Integer, Double> distances;

	/**
	 * Constructor takes the searcher. This class needs to be instantiated for each search performed, and is certainly not thread safe.
	 * 
	 * @param searcher the searcher, with geolocation data in it, that we will perform the distance search on
	 */
	public SearchSpatial(final Searcher searcher) {
		super(searcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		DistanceQueryBuilder queryBuilder = new DistanceQueryBuilder(coordinate.getLat(), coordinate.getLon(), distance, IConstants.LAT,
				IConstants.LNG, CartesianTierPlotter.DEFALT_FIELD_PREFIX, Boolean.TRUE);
		// As the radius filter has performed the distance calculations
		// already, pass in the filter to reuse the results
		DistanceFieldComparatorSource fieldComparator = new DistanceFieldComparatorSource(queryBuilder.getDistanceFilter());
		// Create a distance sort
		Sort sort = new Sort(new SortField("geo_distance", fieldComparator));
		TopDocs topDocs = searcher.search(query, queryBuilder.getFilter(), maxResults, sort);
		distances = queryBuilder.getDistanceFilter().getDistances();
		if (logger.isDebugEnabled()) {
			logger.debug("Distances : " + distances);
		}
		return topDocs;
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<HashMap<String, String>> execute() {
		if (searcher == null) {
			logger.warn("No searcher on any index, is an index created?");
		}
		long totalHits = 0;
		ArrayList<HashMap<String, String>> results = null;
		long start = System.currentTimeMillis();
		try {
			Query query = getQuery();
			TopDocs topDocs = search(query);
			totalHits = topDocs.totalHits;
			results = getResults(topDocs, query);
			for (int i = 0; i < maxResults && i < topDocs.totalHits && i < topDocs.scoreDocs.length; i++) {
				final int docID = topDocs.scoreDocs[i].doc;
				double distanceFromOrigin = distances.get(docID);
				Map<String, String> result = results.get(i);
				result.put(IConstants.DISTANCE, Double.toString(distanceFromOrigin));
			}
		} catch (Exception e) {
			logger.error("Exception searching for string " + searchStrings[0] + " in searcher " + searcher, e);
			if (results == null) {
				results = new ArrayList<HashMap<String, String>>();
			}
		}
		long duration = System.currentTimeMillis() - start;
		// Add the search results size as a last result
		addStatistics(results, totalHits, duration);
		return results;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

}