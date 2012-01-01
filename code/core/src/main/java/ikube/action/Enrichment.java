package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.geospatial.GeoName;
import ikube.service.ISearcherWebService;

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
	// @Qualifier("ikube.database.IDataBase.GeoSpatial")
	private IDataBase dataBase;
	@Autowired
	private ISearcherWebService searcherWebService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> context) throws Exception {
		logger.info("Running the enrichment : ");
		ikube.model.Action action = null;
		try {
			action = start("enrichment", "enrichment");
			// List all the entities in the geoname table
			int id = 0;
			int batch = 10000;
			int exceptions = 0;
			int maxExceptions = 1000;
			List<GeoName> geoNames = new ArrayList<GeoName>();
			String[] searchStrings = new String[3];
			do {
				try {
					for (GeoName geoName : geoNames) {
						if (geoName.getCity() == null) {
							setCity(searcherWebService, geoName, searchStrings);
						}
						if (geoName.getCountry() == null) {
							setCountry(searcherWebService, geoName, searchStrings);
						}
					}
					// Merge the entity batch
					dataBase.mergeBatch(geoNames);
					geoNames = dataBase.find(GeoName.class, GeoName.SELECT_FROM_GEONAME_BY_CITY_AND_COUNTRY_NULL, new String[] {},
							new Object[] {}, id, batch);
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
				} finally {
					id += batch;
				}
			} while (true);
		} finally {
			stop(action);
		}
		return Boolean.TRUE;
	}

	protected void setCity(final ISearcherWebService searcherWebService, final GeoName geoName, final String[] searchStrings) {
		// Search the geoname index for the closest city
		searchStrings[0] = CITY_FEATURE_CLASS;
		searchStrings[1] = CITY_FEATURE_CODE;
		searchStrings[2] = geoName.getCountryCode();
		double latitude = geoName.getLatitude();
		double longitude = geoName.getLongitude();
		ArrayList<HashMap<String, String>> results = searcherWebService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings,
				SEARCH_FIELDS, Boolean.TRUE, 0, 10, 15, latitude, longitude);
		if (results.size() > 1) {
			// Find the result that is a city close to the feature
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
			// logger.info("City : {} {} ", city, geoName);
			geoName.setCity(city);
		}
	}

	protected void setCountry(final ISearcherWebService searcherWebService, final GeoName geoName, final String[] searchStrings) {
		searchStrings[0] = COUNTRY_FEATURE_CLASS;
		searchStrings[1] = COUNTRY_FEATURE_CODE;
		searchStrings[2] = geoName.getCountryCode();
		ArrayList<HashMap<String, String>> results = searcherWebService.searchMulti(IConstants.GEOSPATIAL, searchStrings, SEARCH_FIELDS,
				Boolean.TRUE, 0, 10);
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
			// logger.info("Country : {} {} ", country, geoName);
			geoName.setCountry(country);
		}
	}

}