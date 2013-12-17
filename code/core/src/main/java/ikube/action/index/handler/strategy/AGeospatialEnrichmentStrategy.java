package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.model.Coordinate;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Michael Couck
 * @since 18.12.2013
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public abstract class AGeospatialEnrichmentStrategy extends AStrategy {

	@Value("${start.tier}")
	transient int startTierParam = 10;
	@Value("${end.tier}")
	transient int endTierParam = 20;

	transient int startTier;
	transient int endTier;

	/** No idea what this does :) */
	transient IProjector sinusodialProjector;
	/** The geocoder to get the co-ordinates for the indexable. */
	@Autowired
	IGeocoder geocoder;

	public AGeospatialEnrichmentStrategy() {
		this(null);
	}

	public AGeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
		initialize();
	}

	public final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLatitude()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLongitude()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document);
		// logger.info("Adding location fields : " + document);
	}

	final void addCartesianTiers(final Coordinate coordinate, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			// logger.info("Adding cartesian tiers : ");
			CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(tier, sinusodialProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = cartesianTierPlotter.getTierBoxId(coordinate.getLatitude(), coordinate.getLongitude());
			document.add(new Field(cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
		// logger.info("Adding cartesian tiers : " + document);
	}

	public void initialize() {
		sinusodialProjector = new SinusoidalProjector();
		CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(0, sinusodialProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		startTier = cartesianTierPlotter.bestFit(startTierParam);
		endTier = cartesianTierPlotter.bestFit(endTierParam);
	}

}