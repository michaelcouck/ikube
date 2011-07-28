package ikube.index.spatial.enrich;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.toolkit.Logging;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;

/**
 * @see IEnrichment
 * @author Michael Couck
 * @since 12.04.11
 * @version 01.00
 */
public class Enrichment implements IEnrichment {

	private static final Logger LOGGER = Logger.getLogger(Enrichment.class);

	private transient int endTier;
	private transient int startTier;
	private transient IProjector projector;
	private transient CartesianTierPlotter cartesianTierPlotter;

	public Enrichment() {
		projector = new SinusoidalProjector();
		cartesianTierPlotter = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addSpatialLocationFields(Coordinate coordinate, Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document, startTier, endTier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addCartesianTiers(Coordinate coordinate, Document document, int startTier, int endTier) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(tier, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = cartesianTierPlotter.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Logging.getString("Tier : ", tier, ", box id : ", boxId, ", cartesian tier : ",
						ToStringBuilder.reflectionToString(cartesianTierPlotter, ToStringStyle.SHORT_PREFIX_STYLE)));
			}
			document.add(new Field(cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Coordinate getCoordinate(Indexable<?> indexable) {
		double latitude = Double.MAX_VALUE;
		double longitude = Double.MAX_VALUE;
		Coordinate coordinate = null;
		for (Indexable<?> child : indexable.getChildren()) {
			try {
				if (!IndexableColumn.class.isAssignableFrom(child.getClass())) {
					continue;
				}
				Object content = ((IndexableColumn) child).getContent();
				if (content == null) {
					continue;
				}
				if (child.getName().equals(IConstants.LATITUDE)) {
					latitude = Double.parseDouble(content.toString());
				} else if (child.getName().equals(IConstants.LONGITUDE)) {
					longitude = Double.parseDouble(content.toString());
				}
			} catch (Exception e) {
				LOGGER.error("Exception getting the lat/long co-ordinates : " + indexable, e);
			}
		}
		if (latitude != Double.MAX_VALUE && longitude != Double.MAX_VALUE) {
			coordinate = new Coordinate(latitude, longitude);
		}
		// LOGGER.info("Co-ordinate : " + coordinate);
		return coordinate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringBuilder buildAddress(Indexable<?> indexable, StringBuilder builder) {
		if (IndexableColumn.class.isAssignableFrom(indexable.getClass()) && indexable.isAddress()) {
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			if (builder.length() > 0) {
				builder.append(" ");
			}
			builder.append(indexableColumn.getContent());
		}
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				buildAddress(child, builder);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Address : " + builder);
		}
		return builder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMinKm(final double minKm) {
		this.startTier = cartesianTierPlotter.bestFit(minKm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMaxKm(final double maxKm) {
		this.endTier = cartesianTierPlotter.bestFit(maxKm);
	}

}