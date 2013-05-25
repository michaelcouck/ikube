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
				URL url = Logging.class.getResource(LOG_4_J_PROPERTIES);
				System.out.println(Logging.class.getName() + " Log4j url : " + url);
				if (url != null) {
					inputStream = url.openStream();
				} else {
					inputStream = Logging.class.getResourceAsStream(LOG_4_J_PROPERTIES);
					System.err.println("Input stream to logging configuration : " + inputStream);
					if (inputStream == null) {
						inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(LOG_4_J_PROPERTIES);
					}
					if (inputStream == null) {
						File log4JPropertiesFile = FileUtilities.findFileRecursively(new File("." + IConstants.SEP + IConstants.IKUBE),
								"log4j.properties");
						if (log4JPropertiesFile != null && log4JPropertiesFile.exists() && log4JPropertiesFile.canRead()) {
							inputStream = log4JPropertiesFile.toURI().toURL().openStream();
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
					LOGGER.info("Searching for log file : " + IConstants.IKUBE_LOG);
					LOG_FILE = FileUtilities.findFileRecursively(new File("."), "ikube\\.log");
					if (LOG_FILE != null) {
						LOGGER.info("Found log file : " + LOG_FILE.getAbsolutePath());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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