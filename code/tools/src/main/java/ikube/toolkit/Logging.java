package ikube.toolkit;

import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class just initializes the logging(Log4j).
 * 
 * @author Michael Couck
 * @since 15.09.10
 * @version 01.00
 */
public final class Logging {

	private static boolean INITIALISED = false;
	private static final String SEP = "/"; // File.separator;
	private static final String META_INF = SEP + "META-INF";
	private static final String LOG_4_J_PROPERTIES = META_INF + SEP + "log4j.properties";

	private Logging() {
	}

	/**
	 * Configures the logging.
	 */
	public static synchronized void configure() {
		if (INITIALISED) {
			return;
		}
		INITIALISED = true;
		try {
			URL url = Logging.class.getResource(LOG_4_J_PROPERTIES);
			System.out.println(Logging.class.getName() + " Log4j url : " + url);
			if (url != null) {
				PropertyConfigurator.configure(url);
			} else {
				System.err.println("Logging properties file not found : " + LOG_4_J_PROPERTIES);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Takes a bunch of objects and concatenates them as a string.
	 * 
	 * @param objects
	 *            the objects to concatenate
	 * @return the string concatenation of the objects
	 */
	public static String getString(final Object... objects) {
		if (objects == null || objects.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		boolean first = Boolean.TRUE;
		for (Object object : objects) {
			if (first) {
				first = Boolean.FALSE;
			} else {
				builder.append(", ");
			}
			builder.append(object);
		}
		return builder.toString();
	}

}
