package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.geospatial.GeoName;
import ikube.service.ISearcherWebService;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class will try to enrich the GeoName table with country and city data. First it will search the GeoName index to find the closest
 * city to the feature and take the top result, then it will select from the database to find the country and set that too.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class Enrichment extends Action<IndexContext<?>, Boolean> implements IConstants {

	public static final String CITY_FEATURE_CLASS = "P S T";
	public static final String CITY_FEATURE_CODE = "PPL PPL PPLA PPLA2 PPLA3 PPLA4 PPLC PPLF PPLG PPLL PPLQ PPLR PPLS PPLW PPLX STLMT";
	public static final String COUNTRY_FEATURE_CLASS = "A";
	public static final String COUNTRY_FEATURE_CODE = "PCLI ADM1 ADM2 ADM3 ADM4 ADMD LTER PCL PCLD PCLF PCLI PCLIX PCLS PRSH TERR ZN ZNB";
	private static final String[] SEARCH_FIELDS = { FEATURECLASS, FEATURECODE, COUNTRYCODE };

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private ISearcherWebService searcherWebService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(IndexContext<?> context) throws Exception {
		logger.info("Running the enrichment : ");
		// List all the entities in the geoname table
		int batch = 100;
		long id = 0;
		int exceptions = 0;
		int maxExceptions = 1000;
		List<GeoName> geoNames = new ArrayList<GeoName>();

		String[] searchStrings = new String[3];
		do {
			try {
				for (GeoName geoName : geoNames) {
					id = geoName.getId();
					if (geoName.getCity() == null) {
						setCity(searcherWebService, geoName, searchStrings);
					}
					if (geoName.getCountry() == null) {
						setCountry(searcherWebService, geoName, searchStrings);
					}
					// Merge the entity
					dataBase.merge(geoName);
				}
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(IConstants.ID, id);
				geoNames = dataBase.find(GeoName.class, GeoName.SELECT_FROM_GEONAME_BY_ID_GREATER_AND_SMALLER, parameters, 0, batch);
				logger.info("Geoname size : " + geoNames.size() + ", " + id);
				if (geoNames.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				logger.error("Exception enriching the GeoName data : ", e);
				exceptions++;
				if (exceptions > maxExceptions) {
					break;
				}
			}
		} while (true);
		return Boolean.TRUE;
	}

	protected void setCity(ISearcherWebService searcherWebService, GeoName geoName, String[] searchStrings) {
		// Search the geoname index for the closest city
		searchStrings[0] = CITY_FEATURE_CLASS;
		searchStrings[1] = CITY_FEATURE_CODE;
		searchStrings[2] = geoName.getCountryCode();
		double latitude = geoName.getLatitude();
		double longitude = geoName.getLongitude();
		String xml = searcherWebService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings, SEARCH_FIELDS, Boolean.TRUE, 0, 10, 15,
				latitude, longitude);
		@SuppressWarnings("unchecked")
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		if (results.size() > 1) {
			// Find the result that is a country, i.e. the feature class with 'T'
			String city = null;
			for (Map<String, String> result : results) {
				String featureclass = result.get(FEATURECLASS);
				String featurecode = result.get(FEATURECODE);
				if (featureclass == null || featurecode == null) {
					continue;
				}
				if (CITY_FEATURE_CLASS.contains(featureclass) && CITY_FEATURE_CODE.contains(featurecode)) {
					city = result.get(ASCIINAME);
					break;
				}
			}
			if (city == null) {
				city = results.get(0).get(ASCIINAME);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(Logging.getString("City : ", city, geoName));
			}
			geoName.setCity(city);
		}
	}

	protected void setCountry(ISearcherWebService searcherWebService, GeoName geoName, String[] searchStrings) {
		searchStrings[0] = COUNTRY_FEATURE_CLASS;
		searchStrings[1] = COUNTRY_FEATURE_CODE;
		searchStrings[2] = geoName.getCountryCode();
		String xml = searcherWebService.searchMulti(IConstants.GEOSPATIAL, searchStrings, SEARCH_FIELDS, Boolean.TRUE, 0, 10);
		@SuppressWarnings("unchecked")
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		if (results.size() > 1) {
			// Find the result that is a country, i.e. the feature class with 'T'
			String country = null;
			for (Map<String, String> result : results) {
				String featureclass = result.get(FEATURECLASS);
				String featurecode = result.get(FEATURECODE);
				if (featureclass == null || featurecode == null) {
					continue;
				}
				if (COUNTRY_FEATURE_CLASS.contains(featureclass) && COUNTRY_FEATURE_CODE.contains(featurecode)) {
					country = result.get(ASCIINAME);
					break;
				}
			}
			if (country == null) {
				country = results.get(0).get(ASCIINAME);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(Logging.getString("Country : ", country, geoName));
			}
			geoName.setCountry(country);
		}
	}

}