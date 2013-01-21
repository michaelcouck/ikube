package ikube.index.handler.strategy;

import ikube.IConstants;
import ikube.index.handler.IStrategy;
import ikube.index.spatial.Coordinate;
import ikube.index.spatial.geocode.IGeocoder;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * TODO Document me.
 * 
 * @author Michael Couck
 * @since 20.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public final class GeospatialEnrichmentStrategy extends AStrategy {

	@Value("${end.tier}")
	private transient int endTier;
	@Value("${start.tier}")
	private transient int startTier;
	/** No idea what this does :) */
	private transient IProjector sinusodialProjector;
	/** The geocoder to get the co-ordinates for the indexable. */
	@Autowired
	private IGeocoder geocoder;

	public GeospatialEnrichmentStrategy() {
		this(null);
	}

	public GeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
		sinusodialProjector = new SinusoidalProjector();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final Object... parameters) throws Exception {
		boolean mustProceed = Boolean.TRUE;
		// The parameters can be either the columns and values from a csv file
		// or the columns from a table filled in with the values. All the logic from the Enrichment class can
		// be used in here to keep all the enrichment logic in the same place
		Indexable<?> indexable = (Indexable<?>) parameters[1];
		Document document = (Document) parameters[2];
		Coordinate coordinate = getCoordinate(indexable);
		if (coordinate != null) {
			addSpatialLocationFields(coordinate, document);
		}
		return mustProceed && super.aroundProcess(parameters);
	}

	private Coordinate getCoordinate(final Indexable<?> indexable) {
		Double latitude = null;
		Double longitude = null;
		for (final Indexable<?> child : indexable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) child;
				if (indexableColumn.isAddress()) {
					Object content = indexableColumn.getContent();
					if (indexableColumn.getName().toLowerCase().contains(IConstants.LATITUDE.toLowerCase())) {
						latitude = Double.parseDouble(content.toString());
					} else if (indexableColumn.getName().toLowerCase().equals(IConstants.LONGITUDE.toLowerCase())) {
						longitude = Double.parseDouble(content.toString());
					}
				}
			}
		}
		if (latitude == null || longitude == null) {
			String address = buildAddress(indexable, new StringBuilder()).toString();
			// The GeoCoder is a last resort in fact
			Coordinate coordinate = geocoder.getCoordinate(address);
			if (coordinate != null) {
				logger.info("Got co-ordinate for : " + indexable.getName() + ", " + coordinate);
				return coordinate;
			}
			logger.warn("Lat and/or long are null, have you configured the columns correctly : ");
			return null;
		}
		return new Coordinate(latitude, longitude);
	}

	private final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document);
	}

	private final void addCartesianTiers(final Coordinate coordinate, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(tier, sinusodialProjector,
					CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = cartesianTierPlotter.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			// LOGGER.info(Logging.getString("Tier : ", tier, ", box id : ", boxId, ", cartesian tier : ",
			// ToStringBuilder.reflectionToString(cartesianTierPlotter, ToStringStyle.SHORT_PREFIX_STYLE)));
			document.add(new Field(cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	private final StringBuilder buildAddress(final Indexable<?> indexable, final StringBuilder builder) {
		if (indexable.isAddress()) {
			if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) indexable;
				if (builder.length() > 0) {
					builder.append(" ");
				}
				builder.append(indexableColumn.getContent());
			}
		}
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				buildAddress(child, builder);
			}
		}
		return builder;
	}

}