package ikube.tool.geospatial;

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

public class GeonamePopulator {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(GeonamePopulator.class);

	public static void main(String[] args) {
		String sessionName = "geoname8";
		Session session = SessionFactory.getSession(sessionName);
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		ThreadUtilities.destroy();
		int batchSize = 1000;
		List<GeoName> geoNames = new ArrayList<GeoName>();
		while (session.hasNext(GeoName.class)) {
			try {
				GeoName geoName = session.next(GeoName.class);
				geoNames.add(geoName);
				if (geoNames.size() >= batchSize) {
					LOGGER.info("Persisting batch : " + geoNames.size());
					dataBase.persistBatch(geoNames);
					geoNames = new ArrayList<GeoName>();
				}
			} catch (Exception e) {
				LOGGER.error("Exception inserting geoname : ", e);
				// Try the geonames one by one
				for (GeoName geoName : geoNames) {
					try {
						dataBase.persist(geoName);
					} catch (Exception ex) {
						LOGGER.error("", ex);
					}
				}
				geoNames = new ArrayList<GeoName>();
			}
		}
	}

}
