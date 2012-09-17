package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.geospatial.GeoName;

import java.util.List;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean executeInternal(final IndexContext<?> context) throws Exception {
		logger.info("Running the enrichment : ");
		ikube.model.Action action = null;
		try {
			action = start("enrichment", "enrichment");
			int id = 0;
			int batch = 10000;
			List<GeoName> geoNames = dataBase.find(GeoName.class, GeoName.SELECT_FROM_GEONAME_BY_CITY_AND_COUNTRY_NULL, new String[] {},
					new Object[] {}, id, batch);
			while (!geoNames.isEmpty()) {
				logger.info("Geoname size : " + geoNames.size() + ", " + id);
				for (GeoName geoName : geoNames) {
					if (geoName.getCity() == null) {
						setCity(geoName);
					}
					if (geoName.getCountry() == null) {
						setCountry(geoName);
					}
				}
				// Merge the entity batch
				dataBase.mergeBatch(geoNames);
				geoNames.clear();
				action.setInvocations(id);
				id += batch;
				geoNames = dataBase.find(GeoName.class, GeoName.SELECT_FROM_GEONAME_BY_CITY_AND_COUNTRY_NULL, new String[] {},
						new Object[] {}, id, batch);
			}
		} finally {
			stop(action);
		}
		return Boolean.TRUE;
	}

	private String[] names = new String[3];
	private Object[] values = new Object[3];

	protected void setCity(final GeoName geoName) {

	}

	protected void setCountry(final GeoName geoName) {
		values[0] = geoName.getCountryCode();
		values[1] = geoName.getLongitude();
		values[2] = geoName.getLatitude();
		List<GeoName> countryGeoNames = dataBase.find(GeoName.class, GeoName.SELECT_FROM_GEONAME_BY_FEATURECLASS_FEATURECODE_COUNTRYCODE,
				names, values, 0, 10);
		// Now find the closest to the target co-ordinate
	}

}