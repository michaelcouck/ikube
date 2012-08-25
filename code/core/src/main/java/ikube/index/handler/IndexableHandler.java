package ikube.index.handler;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.spatial.Coordinate;
import ikube.index.spatial.enrich.IEnrichment;
import ikube.index.spatial.geocode.IGeocoder;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for the handlers that contains access to common functionality like the threads etc.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class IndexableHandler<T extends Indexable<?>> implements IHandler<T> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The number of threads that this handler will spawn. */
	private int threads;
	/** The class that this handler can handle. */
	private Class<T> indexableClass;
	/** Access to the database. */
	@Autowired
	protected IDataBase dataBase;
	/** The geocoder to get the co-ordinates for the indexable. */
	@Autowired
	private IGeocoder geocoder;
	/** The enricher that will add the spatial tiers to the document. */
	@Autowired
	private IEnrichment enrichment;
	@Autowired
	private IClusterManager clusterManager; 

	public int getThreads() {
		return threads;
	}

	public void setThreads(final int threads) {
		this.threads = threads;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getIndexableClass() {
		return indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexableClass(final Class<T> indexableClass) {
		this.indexableClass = indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDocument(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document) throws Exception {
		if (indexable.isAddress()) {
			addSpatialEnrichment(indexable, document);
		}
		indexContext.getIndexWriter().addDocument(document);
	}

	private void addSpatialEnrichment(Indexable<?> indexable, Document document) {
		// We look for the first latitude and longitude from the children
		Coordinate coordinate = enrichment.getCoordinate(indexable);
		// If the coordinate is null then either there were no latitude and longitude
		// indexable children in the address indexable or there was a data problem, so we will
		// see if there is a geocoder to get the coordinate
		if (coordinate == null) {
			String address = enrichment.buildAddress(indexable, new StringBuilder()).toString();
			try {
				// The GeoCoder is a last resort in fact
				coordinate = geocoder.getCoordinate(address);
			} catch (Exception e) {
				logger.error("Exception accessing the geocoder : " + geocoder + ", " + address, e);
			}
			if (coordinate == null) {
				return;
			}
			logger.info("Got co-ordinate for : " + indexable.getName() + ", " + coordinate);
		}
		enrichment.addSpatialLocationFields(coordinate, document);
	}

}