package ikube.action.index.handler.strategy;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.model.Coordinate;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18.12.2013
 */
public abstract class AGeospatialEnrichmentStrategy extends AStrategy {

	@Value("${max.geohash.levels}")
	transient int maxGeohashLevels = IConstants.MAX_GEOHASH_LEVELS;

	/**
	 * The geocoder to get the co-ordinates for the indexable.
	 */
	@Autowired
	IGeocoder geocoder;

	SpatialContext spatialContext;
	SpatialStrategy spatialStrategy;

	public AGeospatialEnrichmentStrategy() {
		this(null);
	}

	public AGeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	public final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		// Note to self: This takes an x and y co-ordinate so the
		// order must be longitude(x) and latitude(y), not the other way
		Shape shape = spatialContext.makePoint(coordinate.getLongitude(), coordinate.getLatitude());
		for (IndexableField indexableField : spatialStrategy.createIndexableFields(shape)) {
			if (logger.isDebugEnabled()) {
				logger.info("Adding spatial field : {} ", indexableField);
			}
			document.add(indexableField);
		}
		// Store this field any way
		document.add(new StoredField(spatialStrategy.getFieldName(), spatialContext.toString(shape)));
	}

	public void initialize() {
		spatialContext = SpatialContext.GEO;
		SpatialPrefixTree spatialPrefixTree = new GeohashPrefixTree(spatialContext, maxGeohashLevels);
		this.spatialStrategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, IConstants.POSITION_FIELD_NAME);
	}

}