package ikube.geospatial;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.geospatial.GeoName;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
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

	static String path = "/ikube/service/search/single";
	static String[] names = { IConstants.INDEX_NAME, IConstants.SEARCH_STRINGS, IConstants.SEARCH_FIELDS, IConstants.FRAGMENT,
			IConstants.FIRST_RESULT, IConstants.MAX_RESULTS };
	static HttpClient httpClient = new HttpClient();
	static String[] VALUES = { IConstants.GEOSPATIAL, null, IConstants.NAME, Boolean.TRUE.toString(), "0", "10" };
	static String URL;

	static {
		Logging.configure();
		LOGGER = LoggerFactory.getLogger(GeonamePopulator.class);
		WebServiceAuthentication.authenticate(httpClient, "localhost", 8080, "user", "user");
		try {
			URL = new URL("http", "localhost", 8080, path).toString();
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
	}

	public static void main(String[] args) throws Exception {
		persist();
	}

	protected static void verify() throws Exception {
		// IConstants.SEP + IConstants.IKUBE + Searcher.SERVICE + Searcher.SEARCH + Searcher.SINGLE;
		String sessionName = "geoname";
		Session session = SessionFactory.getSession(sessionName);
		int line = 0;
		while (session.hasNext(GeoName.class)) {
			line++;
			try {
				GeoName geoName = session.next(GeoName.class);
				if (!getResult(geoName)) {
					LOGGER.info("Line : " + line + ", not found : " + geoName);
				}
				if (line % 1000 == 0) {
					LOGGER.info("Line : " + line);
				}
			} catch (Exception e) {
				LOGGER.error("Exception verifying geoname : ", e);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	protected static boolean getResult(final GeoName geoName) throws Exception {
		VALUES[1] = geoName.getName();
		NameValuePair[] params = getNameValuePairs(names, VALUES);
		GetMethod getMethod = new GetMethod(URL);
		getMethod.setQueryString(params);
		int webResult = httpClient.executeMethod(getMethod);
		String actual = FileUtilities.getContents(getMethod.getResponseBodyAsStream(), Integer.MAX_VALUE).toString();
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(actual);
		if (results.size() > 1) {
			for (int i = 0; i < results.size() - 1; i++) {
				Map<String, String> result = results.get(i);
				String name = result.get(IConstants.NAME);
				if (name != null && geoName.getName().equals(name)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	protected static NameValuePair[] getNameValuePairs(String[] names, String[] values) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < names.length && i < values.length; i++) {
			NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
			nameValuePairs.add(nameValuePair);
		}
		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
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
				if (count % 1000 == 0) {
					LOGGER.info("Count : " + count);
				}
				GeoName geoName = session.next(GeoName.class);
				GeoName dbGeoName = dataBase.find(GeoName.class, GeoName.SELECT_FROM_GEONAME_BY_GEONAMEID, new String[] { "geonameid" },
						new Object[] { geoName.getGeonameid() });
				if (dbGeoName != null) {
					continue;
				}
				LOGGER.info("Didn't find : " + geoName);
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
