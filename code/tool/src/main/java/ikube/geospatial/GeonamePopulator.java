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
		persist(GeoName.class);
		// persist(AlternateName.class);
	}

	protected static void persist(final Class<?> clazz) {
		String sessionName = "geoname";
		Session session = SessionFactory.getSession(sessionName);
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		ThreadUtilities.destroy();
		int batchSize = 1000;
		List<Object> geoNames = new ArrayList<Object>();
		int count = 0;
		while (session.hasNext(clazz)) {
			count++;
			try {
				Object geoName = session.next(clazz);
				if (count % 10000 == 0) {
					LOGGER.info("Count : " + count);
				}
				geoNames.add(geoName);
				if (geoNames.size() >= batchSize || !session.hasNext(clazz)) {
					persistBatch(dataBase, geoNames);
				}
			} catch (Exception e) {
				LOGGER.error("Exception inserting geoname : ", e);
				persistBatch(dataBase, geoNames);
			}
		}
	}

	private static void persistBatch(IDataBase dataBase, List<Object> geoNames) {
		try {
			// LOGGER.info("Persisting batch : " + geoNames.size());
			dataBase.persistBatch(geoNames);
			geoNames.clear();
		} catch (Exception e) {
			LOGGER.error("Exception inserting geoname : ", e);
		} finally {
			for (Object geoName : geoNames) {
				try {
					dataBase.persist(geoName);
				} catch (Exception ex) {
					LOGGER.error(ex.getMessage());
				}
			}
			geoNames.clear();
		}
	}

}