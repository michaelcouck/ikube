package ikube.action.index.handler.strategy;

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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

/**
 * @author Michael Couck
 * @since 18.12.2013
 * @version 01.00
 */
public abstract class AGeospatialEnrichmentStrategy extends AStrategy {

	@Value("${max.geohash.levels}")
	transient int maxGeohashLevels = IConstants.MAX_GEOHASH_LEVELS;

	/** The geocoder to get the co-ordinates for the indexable. */
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
		Shape shape = spatialContext.makePoint(coordinate.getLatitude(), coordinate.getLongitude());
		for (IndexableField indexableField : spatialStrategy.createIndexableFields(shape)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding spatial field : {} ", indexableField);
			}
			document.add(indexableField);
		}
		// We must store this field to be able to sort the results by distance
		document.add(new StoredField(spatialStrategy.getFieldName(), spatialContext.toString(shape)));
	}

	public void initialize() {
		spatialContext = SpatialContext.GEO;
		SpatialPrefixTree spatialPrefixTree = new GeohashPrefixTree(spatialContext, maxGeohashLevels);
		this.spatialStrategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, IConstants.POSITION_FIELD_NAME);
	}

}