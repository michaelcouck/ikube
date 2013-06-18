package ikube.toolkit;

import ikube.Constants;

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
public final class Logging implements Constants {

	private static Logger LOGGER;
	private static boolean INITIALISED = false;
	private static File LOG_FILE;

	/**
	 * Singularity.
	 */
	private Logging() {
		// Documented
	}

	/**
	 * Configures the logging.
	 */
	public static void configure() {
		InputStream inputStream = null;
		try {
			if (INITIALISED) {
				return;
			}
			INITIALISED = Boolean.TRUE;
			try {
				// First check the external logging properties file
				File log4JPropertiesFile = FileUtilities.findFileRecursively(new File("." + SEP + IKUBE), "log4j.properties");
				System.out.println(Logging.class.getName() + " Log4j file : " + log4JPropertiesFile);
				if (log4JPropertiesFile != null && log4JPropertiesFile.exists() && log4JPropertiesFile.canRead()) {
					inputStream = log4JPropertiesFile.toURI().toURL().openStream();
				}
				if (inputStream == null) {
					// Try the class loader
					URL url = Logging.class.getResource(LOG_4_J_PROPERTIES);
					System.out.println(Logging.class.getName() + " Log4j url : " + url);
					if (url != null) {
						inputStream = url.openStream();
					} else {
						// Nope, try the class loader on a stream
						inputStream = Logging.class.getResourceAsStream(LOG_4_J_PROPERTIES);
						System.err.println("Input stream to logging configuration : " + inputStream);
						if (inputStream == null) {
							// Finally try the system class loader
							inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(LOG_4_J_PROPERTIES);
						}
					}
				}
				System.out.println("Log for J : " + inputStream);
				if (inputStream != null) {
					Properties properties = new Properties();
					properties.load(inputStream);
					PropertyConfigurator.configure(properties);
				} else {
					System.err.println("Logging properties file not found : " + LOG_4_J_PROPERTIES);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			LOGGER = Logger.getLogger(Logging.class);
			try {
				if (LOG_FILE == null) {
					LOGGER.info("Searching for log file : " + IKUBE_LOG);
					LOG_FILE = FileUtilities.findFileRecursively(new File("."), "ikube\\.log");
					if (LOG_FILE != null) {
						LOGGER.info("Found log file : " + LOG_FILE.getAbsolutePath());
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception looking for the log file : ", e);
			}
		} finally {
			FileUtilities.close(inputStream);
		}
	}

	public static synchronized File getLogFile() {
		try {
			return LOG_FILE;
		} finally {
			Logging.class.notifyAll();
		}
	}

}