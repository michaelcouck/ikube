package ikube.search;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import ikube.IConstants;
import ikube.model.Coordinate;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.spatial.util.CachingDoubleValueSource;
import org.apache.lucene.spatial.vector.DistanceValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static ikube.IConstants.DISTANCE;

/**
 * Executes a spatial search. One of these classes needs to be instantiated for each search as the member variables are for a particular search.
 *
 * @author Michael Couck
 * @version 01.00
 * @see Search
 * @since 06.03.11
 */
public class SearchSpatial extends SearchComplex {

	/**
	 * The distance from the origin that we will accept in the results.
	 */
	protected transient int distance;
	/**
	 * The origin, i.e. the starting point for the distance search.
	 */
	protected transient Coordinate coordinate;
	protected SpatialStrategy spatialStrategy;
	protected SpatialPrefixTree spatialPrefixTree;
	/**
	 * The spatial context for calculating distance and so on.
	 */
	protected transient SpatialContext spatialContext;

	/**
	 * Constructor takes the searcher. This class needs to be instantiated for each search performed, and is certainly not thread safe.
	 *
	 * @param searcher the searcher, with geolocation data in it, that we will perform the distance search on
	 */
	public SearchSpatial(final IndexSearcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchSpatial(final IndexSearcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
		spatialContext = SpatialContext.GEO;
		spatialPrefixTree = new GeohashPrefixTree(spatialContext, IConstants.MAX_GEOHASH_LEVELS);
		spatialStrategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, IConstants.POSITION_FIELD_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Coordinate : " + coordinate);
		}
		Point origin = getOrigin();
		// Get the sort field to sort by distance
		double degToKm = DistanceUtils.degrees2Dist(1, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		ValueSource valueSource = spatialStrategy.makeDistanceValueSource(origin, degToKm);//the distance (in km)
		CachingDoubleValueSource cachingDoubleValueSource = new CachingDoubleValueSource(valueSource);
		SortField sortField = cachingDoubleValueSource.getSortField(false);
		Sort distSort = new Sort(sortField).rewrite(searcher);//false=asc dist

		// Reduce the results by maximum distance to the origin
		double degrees = DistanceUtils.dist2Degrees(distance, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		Circle circle = spatialContext.makeCircle(origin.getX(), origin.getY(), degrees);
		SpatialArgs spatialArgs = new SpatialArgs(SpatialOperation.Intersects, circle);
		Filter filter = spatialStrategy.makeFilter(spatialArgs);

		// Reduce the results by the search string
		Query baseQuery = null;
		try {
			baseQuery = super.getQuery();
		} catch (ParseException e) {
			logger.error(null, e);
		} finally {
			if (baseQuery == null) {
				baseQuery = new MatchAllDocsQuery();
			}
		}

		// And hup...
		return searcher.search(baseQuery, filter, maxResults, distSort);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<HashMap<String, String>> execute() {
		ArrayList<HashMap<String, String>> results = super.execute();
		Point point = getOrigin();
		// Pop the statistics off the list
		HashMap<String, String> statistics = results.remove(results.size() - 1);
		for (final HashMap<String, String> result : results) {
			// Apparently we can get the distance from the {@link CachingDoubleValueSource} somehow...
			double latitude = Double.parseDouble(result.get(IConstants.LATITUDE));
			double longitude = Double.parseDouble(result.get(IConstants.LONGITUDE));
			double distanceDegrees = spatialContext.getDistCalc().distance(point, longitude, latitude);
			double distanceKilometres = DistanceUtils.degrees2Dist(distanceDegrees, DistanceUtils.EARTH_MEAN_RADIUS_KM);
			result.put(DISTANCE, Double.toString(distanceKilometres));
		}
		results.add(statistics);
		return results;
	}

	private Point getOrigin() {
		double latitude = coordinate.getLatitude();
		double longitude = coordinate.getLongitude();
		// Note to self: This takes an x and y co-ordinate so the
		// order must be longitude(x) and latitude(y), not the other way
		return spatialContext.makePoint(longitude, latitude);
	}

	public void setDistance(final int distance) {
		this.distance = distance;
	}

	public void setCoordinate(final Coordinate coordinate) {
		this.coordinate = coordinate;
	}

}