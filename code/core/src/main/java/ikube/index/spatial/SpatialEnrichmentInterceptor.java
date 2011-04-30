package ikube.index.spatial;

import ikube.index.spatial.enrich.IEnrichment;
import ikube.index.spatial.geocode.IGeocoder;
import ikube.model.Indexable;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
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

	private transient IGeocoder geocoder;
	private transient IEnrichment enrichment;

	@Override
	public Object enrich(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// Iterate through all the indexable children of the indexable looking for address
		// fields. Concatenate them with a ',' in between. Call the Google geocoding API
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
			LOGGER.info("Not address : " + indexable);
			return;
		}
		LOGGER.info("Enriching : " + indexable);
		// We look for the first latitude and longitude from the children
		Coordinate coordinate = enrichment.getCoordinate(indexable);
		// If the coordinate is null then either there were no latitude and longitude
		// indexable children in the address indexable or there was a data problem, so we will
		// see if there is a geocoder to get the coordinate
		if (coordinate == null) {
			String address = enrichment.buildAddress(indexable, new StringBuilder()).toString();
			try {
				coordinate = geocoder.getCoordinate(address);
			} catch (Exception e) {
				LOGGER.error("Exception accessing the geocoder : " + geocoder + ", " + address, e);
			}
			if (coordinate == null) {
				return;
			}
		}
		enrichment.addSpatialLocationFields(coordinate, document);
	}

	public void setEnrichment(final IEnrichment enrichment) {
		this.enrichment = enrichment;
	}

	public void setGeocoder(final IGeocoder geocoder) {
		this.geocoder = geocoder;
	}

}