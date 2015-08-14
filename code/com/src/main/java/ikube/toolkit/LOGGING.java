package ikube.toolkit;

import ikube.Constants;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * This class just initializes the logging(Log4j).
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-09-2010
 */
public final class LOGGING implements Constants {

    @SuppressWarnings("FieldCanBeLocal")
    private static Logger LOGGER;
    private static boolean INITIALISED = false;
    private static File LOG_FILE;

    /**
     * Singularity.
     */
    private LOGGING() {
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
                File log4JPropertiesFile = FILE.findFileRecursively(new File("." + SEP + IKUBE), "log4j.properties");
                if (log4JPropertiesFile == null) {
                    log4JPropertiesFile = FILE.findFileRecursively(new File("."), "log4j.properties");
                }
                if (log4JPropertiesFile != null && log4JPropertiesFile.exists() && log4JPropertiesFile.canRead()) {
                    inputStream = log4JPropertiesFile.toURI().toURL().openStream();
                    System.out.println("Logging configuration : " + log4JPropertiesFile.toURI().toURL());
                }
                if (inputStream == null) {
                    // Try the class loader
                    URL url = LOGGING.class.getResource(LOG_4_J_PROPERTIES);
                    if (url != null) {
                        inputStream = url.openStream();
                        System.out.println("Logging configuration : " + url);
                    } else {
                        // Nope, try the class loader on a stream
                        inputStream = LOGGING.class.getResourceAsStream(LOG_4_J_PROPERTIES);
                        if (inputStream == null) {
                            // Finally try the system class loader
                            inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(LOG_4_J_PROPERTIES);
                            System.out.println("Logging configuration : System class loader.");
                        }
                    }
                }
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
            LOGGER = Logger.getLogger(LOGGING.class);
            try {
                if (LOG_FILE == null) {
                    LOGGER.info("Searching for log file : " + IKUBE_LOG);
                    LOG_FILE = FILE.findFileRecursively(new File("."), "ikube\\.log");
                    if (LOG_FILE != null) {
                        LOGGER.info("Found log file : " + LOG_FILE.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception looking for the log file : ", e);
            }
        } finally {
            FILE.close(inputStream);
        }
    }

    public static synchronized File getLogFile() {
        try {
            return LOG_FILE;
        } finally {
            LOGGING.class.notifyAll();
        }
    }

}