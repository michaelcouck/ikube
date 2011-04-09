package ikube.index.spatial;

import ikube.IConstants;
import ikube.index.spatial.geocode.IGeocoder;
import ikube.model.Indexable;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * This class will intercept the add document method of the handlers, look through the indexables that they are indexing and accumulate all
 * the address fields. These fields will be fed into one of the geocoding classes that is defined in the configuration. The location in
 * latitude and longitude will be added to the document in Lucene before it is written.
 * 
 * @author Michael Couck
 * @since 06.03.2011
 * @version 01.00
 */
public class SpatialEnrichmentInterceptor implements ISpatialEnrichmentInterceptor {

	private static final Logger LOGGER = Logger.getLogger(SpatialEnrichmentInterceptor.class);
	private transient IProjector projector = new SinusoidalProjector();
	private transient IGeocoder geocoder;
	private transient int startTier;
	private transient int endTier;

	@Override
	public Object enrich(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// Iterate through all the indexable children of the indexable looking for address
		// fields. Concatenate them with a ',' in between. Call the Google geo coding API
		// for the latitude and longitude coordinates. Create the tiers for the location,
		// and add the resultant data to the document, simple.
		enrich(proceedingJoinPoint.getArgs());
		return proceedingJoinPoint.proceed();
	}

	protected void enrich(final Object[] arguments) {
		Document document = null;
		Indexable<?> indexable = null;
		if (arguments != null) {
			for (Object argument : arguments) {
				if (argument == null) {
					continue;
				}
				Class<?> klass = argument.getClass();
				if (Document.class.isAssignableFrom(klass)) {
					document = (Document) argument;
				} else if (Indexable.class.isAssignableFrom(klass)) {
					indexable = (Indexable<?>) argument;
				}
			}
		}
		if (!indexable.isAddress()) {
			return;
		}
		try {
			Coordinate coordinate = geocoder.getCoordinate(indexable);
			addSpatialLocationFields(coordinate, document);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	protected void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document, startTier, endTier);
	}

	protected void addCartesianTiers(final Coordinate coordinate, final Document document, final int startTier, final int endTier) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter ctp = new CartesianTierPlotter(tier, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = ctp.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			LOGGER.info("Tier : " + tier + ", " + boxId);
			document.add(new Field(ctp.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	protected int getMinKm(final double minKm) {
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		return ctp.bestFit(minKm);
	}

	protected int getMaxKm(final double maxKm) {
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		return ctp.bestFit(maxKm);
	}

	public void setMinKm(final double minKm) {
		this.startTier = getMinKm(minKm);
	}

	public void setMaxKm(final double maxKm) {
		this.endTier = getMaxKm(maxKm);
	}

	public void setGeocoder(final IGeocoder geocoder) {
		this.geocoder = geocoder;
	}

}