package ikube.action;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.geospatial.GeoName;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * This class will try to enrich the GeoName table with country and city data. First it will search the GeoName index to find the closest
 * city to the feature and take the top result, then it will select from the database to find the country and set that too.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class Enrichment extends Action<IndexContext, Boolean> {

	@Override
	@SuppressWarnings("unchecked")
	public Boolean execute(final IndexContext indexContext) {
		logger.info("Running the enrichment : ");
		getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.TRUE);
		EntityManager entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_DB2).createEntityManager();
		logger.info("Finished indexing the database : ");
		try {
			Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
			List<String> webServiceUrls = server.getWebServiceUrls();
			String webServiceUrl = null;
			for (String serviceUrl : webServiceUrls) {
				if (serviceUrl.contains(ISearcherWebService.class.getSimpleName())) {
					webServiceUrl = serviceUrl;
					break;
				}
			}
			ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, webServiceUrl,
					ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
			// List all the entities in the geoname table
			int index = 0;
			int batch = 1000;
			long id = 8563751;
			int exceptions = 0;
			int maxExceptions = 1000;
			List<GeoName> geoNames = new ArrayList<GeoName>();
			GeoName geoName = null;
			do {
				if (index >= geoNames.size()) {
					index = 0;
					if (entityManager.getTransaction().isActive()) {
						entityManager.flush();
						entityManager.getTransaction().commit();
						entityManager.clear();
					}
					entityManager.getTransaction().begin();
					Query query = entityManager.createNamedQuery(GeoName.SELECT_FROM_GEONAME_BY_ID_GREATER_AND_SMALLER);
					query.setParameter(IConstants.START, id);
					query.setParameter(IConstants.END, id += batch);
					geoNames = query.getResultList();
					logger.info("Geoname size : " + geoNames.size() + ", " + id);
					if (geoNames.size() == 0) {
						break;
					}
					logger.info("Geoname : " + geoName);
				}
				try {
					geoName = geoNames.get(index++);
					if (geoName.getCity() == null) {
						// Search the geoname index for the closest city
						String[] searchFields = { "featureclass", "featurecode", "countrycode" };
						String[] searchStrings = { "P", "PPL", geoName.getCountryCode() };
						double latitude = geoName.getLatitude();
						double longitude = geoName.getLongitude();
						String xml = searcherWebService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings, searchFields,
								Boolean.FALSE, 0, 10, 10, latitude, longitude);
						List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
						if (results.size() > 1) {
							// Add the top hit from the list
							Map<String, String> result = results.get(0);
							String city = result.get(IConstants.NAME);
							geoName.setCity(city);
						}
					}
					if (geoName.getCountry() == null) {
						String[] searchFields = { "featureclass", "featurecode", "countrycode" };
						String[] searchStrings = { "A", "PCLI", geoName.getCountryCode() };
						double latitude = geoName.getLatitude();
						double longitude = geoName.getLongitude();
						String xml = searcherWebService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings, searchFields,
								Boolean.FALSE, 0, 10, 10, latitude, longitude);
						List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
						if (results.size() > 1) {
							// Add the top hit from the list
							Map<String, String> result = results.get(0);
							String country = result.get(IConstants.NAME);
							geoName.setCountry(country);
						}
					}
					// Merge the entity
					entityManager.merge(geoName);
				} catch (Exception e) {
					logger.error("Exception enriching the GeoName data : ", e);
					exceptions++;
					if (exceptions > maxExceptions) {
						break;
					}
				}
			} while (true);
		} finally {
			try {
				if (entityManager != null) {
					if (entityManager.getTransaction().isActive()) {
						if (entityManager.getTransaction().getRollbackOnly()) {
							entityManager.getTransaction().rollback();
						} else {
							entityManager.getTransaction().commit();
						}
					}
				}
			} catch (Exception e) {
				logger.error("Exception comitting or rolling back the transaction : ", e);
			}
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}