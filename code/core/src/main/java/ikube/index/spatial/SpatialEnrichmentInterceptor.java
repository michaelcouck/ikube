package ikube.index.spatial;

import ikube.IConstants;
import ikube.index.spatial.geocode.IGeocoder;
import ikube.model.Indexable;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 06.03.2011
 * @version 01.00
 */
public class SpatialEnrichmentInterceptor implements ISpatialEnrichmentInterceptor {

	private static final Logger LOGGER = Logger.getLogger(SpatialEnrichmentInterceptor.class);
	private transient int startTier;
	private transient int endTier;
	private transient IProjector projector = new SinusoidalProjector();
	private transient IGeocoder geocoder;

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
		IndexWriter indexWriter = null;
		Document document = null;
		Indexable<?> indexable = null;
		if (arguments != null) {
			for (Object argument : arguments) {
				if (argument == null) {
					continue;
				}
				Class<?> klass = argument.getClass();
				if (IndexWriter.class.isAssignableFrom(klass)) {
					indexWriter = (IndexWriter) argument;
				} else if (Document.class.isAssignableFrom(klass)) {
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
			addLocation(indexWriter, document, /* address, */coordinate);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	protected void addLocation(final IndexWriter writer, final Document document, /* final String address, */final Coordinate coordinate)
			throws Exception {
		// document.add(new Field(IConstants.NAME, address, Field.Store.YES, Index.ANALYZED));
		addSpatialLocationFields(coordinate, document);
		writer.addDocument(document);
	}

	protected void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document);
	}

	protected void addCartesianTiers(final Coordinate coordinate, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter ctp = new CartesianTierPlotter(tier, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = ctp.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			document.add(new Field(ctp.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	public void setMinKm(final double minKm) {
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		startTier = ctp.bestFit(minKm);
	}

	public void setMaxKm(final double maxKm) {
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		endTier = ctp.bestFit(maxKm);
	}

	public void setGeocoder(final IGeocoder geocoder) {
		this.geocoder = geocoder;
	}

}