package ikube.geospatial;

import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.uk.hjcs.canyon.session.Session;
import co.uk.hjcs.canyon.session.SessionFactory;

/**
 * This class will read the Geonames data from the CSV file and persist the entities as {@link GeoName} objects in the database using the
 * JPA from Ikube.
 * 
 * @author Michael Couck
 * @since long time
 * @version 01.00
 */
public class GeonamePopulator {

	static Logger LOGGER;

	static {
		Logging.configure();
		LOGGER = LoggerFactory.getLogger(GeonamePopulator.class);
	}

	public static void main(String[] args) throws Exception {
		persist();
	}

	protected static void persist() {
		String sessionName = "geoname";
		Session session = SessionFactory.getSession(sessionName);
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		ThreadUtilities.destroy();
		int batchSize = 10000;
		List<GeoName> geoNames = new ArrayList<GeoName>();
		int count = 0;
		while (session.hasNext(GeoName.class)) {
			count++;
			try {
				GeoName geoName = session.next(GeoName.class);
				if (count % 1000 == 0) {
					LOGGER.info("Count : " + count);
				}
				geoNames.add(geoName);
				if (geoNames.size() >= batchSize || !session.hasNext(GeoName.class)) {
					persistBatch(dataBase, geoNames);
				}
			} catch (Exception e) {
				LOGGER.error("Exception inserting geoname : ", e);
				persistBatch(dataBase, geoNames);
			}
		}
	}

	private static void persistBatch(IDataBase dataBase, List<GeoName> geoNames) {
		try {
			LOGGER.info("Persisting batch : " + geoNames.size());
			dataBase.persistBatch(geoNames);
			geoNames.clear();
		} catch (Exception e) {
			LOGGER.error("Exception inserting geoname : ", e);
		} finally {
			for (GeoName geoName : geoNames) {
				try {
					dataBase.persist(geoName);
				} catch (Exception ex) {
					LOGGER.error("", ex);
				}
			}
			geoNames.clear();
		}
	}

}