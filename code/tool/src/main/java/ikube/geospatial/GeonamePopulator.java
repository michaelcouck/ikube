package ikube.geospatial;

import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;
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
		int batchSize = 10000;
		List<GeoName> geoNames = new ArrayList<GeoName>();
		while (session.hasNext(GeoName.class)) {
			try {
				GeoName geoName = session.next(GeoName.class);
				geoNames.add(geoName);
				if (geoNames.size() >= batchSize) {
					persistBatch(dataBase, geoNames);
				}
			} catch (Exception e) {
				LOGGER.error("Exception inserting geoname : ", e);
				persistBatch(dataBase, geoNames);
			}
		}
		persistBatch(dataBase, geoNames);
	}

	@SuppressWarnings("unused")
	private static void startProcess(Class<?> klass, String[] sessions) throws IOException, InterruptedException {
		for (String session : sessions) {
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
			String classpath = System.getProperty("java.class.path");
			String className = klass.getCanonicalName();
			ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);
			Process process = builder.start();
			process.waitFor();
			process.exitValue();
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
