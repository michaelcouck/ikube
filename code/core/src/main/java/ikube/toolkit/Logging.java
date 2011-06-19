package ikube.toolkit;

import ikube.IConstants;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This class just initializes the logging(Log4j).
 * 
 * @author Michael Couck
 * @since 15.09.10
 * @version 01.00
 */
public final class Logging implements IConstants {

	private static Logger LOGGER;
	private static boolean INITIALISED = false;
	private static File LOG_FILE;

	private Logging() {
	}

	/**
	 * Configures the logging.
	 */
	public static synchronized void configure() {
		try {
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
					InputStream inputStream = Logging.class.getResourceAsStream(LOG_4_J_PROPERTIES);
					System.err.println("Input stream to logging configuration : " + inputStream);
					if (inputStream != null) {
						Properties properties = new Properties();
						properties.load(inputStream);
						PropertyConfigurator.configure(properties);
					} else {
						inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(LOG_4_J_PROPERTIES);
						System.err.println("Input stream to logging configuration : " + inputStream);
						if (inputStream != null) {
							Properties properties = new Properties();
							properties.load(inputStream);
							PropertyConfigurator.configure(properties);
						} else {
							System.err.println("Searching for log configuration file : " + LOG_4_J_PROPERTIES);
							File logFile = FileUtilities.findFileRecursively(new File("."), LOG_4_J_PROPERTIES);
							if (logFile != null) {
								PropertyConfigurator.configure(logFile.toURI().toURL());
							} else {
								System.err.println("Logging properties file not found : " + LOG_4_J_PROPERTIES);
							}
						}
					}
				}
				LOGGER = Logger.getLogger(Logging.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (LOG_FILE == null) {
					LOGGER.info("Searching for log file : " + IConstants.IKUBE_LOG);
					LOG_FILE = FileUtilities.findFileRecursively(new File("."), IConstants.IKUBE_LOG);
					if (LOG_FILE != null) {
						LOGGER.info("Found log file : " + LOG_FILE.getAbsolutePath());
					}
				}
			} catch (Exception e) {

			}
		} finally {
			Logging.class.notifyAll();
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

	public static synchronized File getLogFile() {
		try {
			return LOG_FILE;
		} finally {
			Logging.class.notifyAll();
		}
	}

}