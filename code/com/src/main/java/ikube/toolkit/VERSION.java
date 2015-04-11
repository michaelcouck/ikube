package ikube.toolkit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * This class will read from the Maven properties file, in the war/jar, and make available some
 * properties like the build time and the version of the project.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-07-2013
 */
public class VERSION {

    private static final Logger LOGGER = LoggerFactory.getLogger(ikube.toolkit.VERSION.class);

    private static String VERSION;
    private static String TIMESTAMP;

    /**
     * The version of the project, from the Maven properties file in the jar.
     */
    public static String version() {
        if (VERSION == null) {
            readPomProperties();
        }
        return VERSION;
    }

    /**
     * The timestamp of the build, from the Maven properties file in the jar.
     */
    public static String timestamp() {
        if (TIMESTAMP == null) {
            readPomProperties();
        }
        return TIMESTAMP;
    }

    /**
     * This method will read the pom properties file where the version and the build timestamp are and make
     * them available to the web pages via the static class properties of the same name.
     */
    public static synchronized void readPomProperties() {
        String pomPropertiesFile = "META-INF/maven/ikube/ikube-core/pom.properties";
        InputStream inputStream = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource(pomPropertiesFile, VERSION.class.getClassLoader());
            inputStream = classPathResource.getInputStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            VERSION = properties.getProperty("version");
        } catch (final Exception e) {
            LOGGER.error("Exception reading the Maven properties for the build : " + pomPropertiesFile + ", " + e.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(null, e);
            }
        } finally {
            if (StringUtils.isEmpty(VERSION)) {
                VERSION = " > 5.3.0 ";
            }
            closeQuietly(inputStream);
        }

        try {
            ClassPathResource classPathResource = new ClassPathResource(pomPropertiesFile, VERSION.class.getClassLoader());
            inputStream = classPathResource.getInputStream();
            List<String> lines = IOUtils.readLines(inputStream);
            TIMESTAMP = lines.get(1).replaceAll("#", "");
        } catch (final Exception e) {
            LOGGER.error("Exception reading the Maven properties for the build : " + pomPropertiesFile + ", " + e.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(null, e);
            }
        } finally {
            if (StringUtils.isEmpty(TIMESTAMP)) {
                TIMESTAMP = " > 01/01/2015";
            }
            closeQuietly(inputStream);
        }
    }
}
