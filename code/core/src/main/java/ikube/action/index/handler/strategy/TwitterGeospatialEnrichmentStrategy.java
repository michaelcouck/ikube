package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableTweets;

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
 * @since 11.12.13
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public final class TwitterGeospatialEnrichmentStrategy extends AStrategy {

	@Value("${twitter.start.tier}")
	private transient int startTierParam = 10;
	@Value("${twitter.end.tier}")
	private transient int endTierParam = 20;

	private transient int startTier;
	private transient int endTier;

	/** No idea what this does :) */
	private transient IProjector sinusodialProjector;
	/** The geocoder to get the co-ordinates for the indexable. */
	@Autowired
	private IGeocoder geocoder;

	public TwitterGeospatialEnrichmentStrategy() {
		this(null);
	}

	public TwitterGeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		boolean mustProceed = Boolean.TRUE;
		if (IndexableTweets.class.isAssignableFrom(indexable.getClass())) {
			Coordinate coordinate = ((IndexableTweets) indexable).getCoordinate();
			if (coordinate != null) {
				logger.debug("Tweet coordinate : {} ", coordinate);
				addSpatialLocationFields(coordinate, document);
			}
		}
		return mustProceed && super.aroundProcess(indexContext, indexable, document, resource);
	}

	public final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLatitude()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLongitude()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document);
	}

	final void addCartesianTiers(final Coordinate coordinate, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(tier, sinusodialProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = cartesianTierPlotter.getTierBoxId(coordinate.getLatitude(), coordinate.getLongitude());
			document.add(new Field(cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	public void initialize() {
		sinusodialProjector = new SinusoidalProjector();
		CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(0, sinusodialProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		startTier = cartesianTierPlotter.bestFit(startTierParam);
		endTier = cartesianTierPlotter.bestFit(endTierParam);
	}

}