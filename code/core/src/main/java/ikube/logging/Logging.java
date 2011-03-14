package ikube.logging;

import ikube.IConstants;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This class just initializes the logging(Log4j).
 * 
 * @author Michael Couck
 * @since 15.09.10
 * @version 01.00
 */
public final class Logging {

	private static Logger LOGGER;
	private static boolean INITIALISED = false;
	
	private Logging() {}

	/**
	 * Configures the logging.
	 */
	public static synchronized void configure() {
		if (INITIALISED) {
			return;
		}
		INITIALISED = true;
		try {
			URL url = Logging.class.getResource(IConstants.LOG_4_J_PROPERTIES);
			System.out.println(Logging.class.getName() + " Log4j url : " + url);
			if (url != null) {
				PropertyConfigurator.configure(url);
			} else {
				System.err.println("Logging properties file not found : " + IConstants.LOG_4_J_PROPERTIES);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER = Logger.getLogger(Logging.class);
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
