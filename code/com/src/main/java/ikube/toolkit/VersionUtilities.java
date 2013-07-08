package ikube.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class VersionUtilities {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtilities.class);
	
	private static String VERSION;
	private static String TIMESTAMP;

	public static String version() {
		if (VERSION == null) {
			readPomProperties();
		}
		return VERSION;
	}

	public static String timestamp() {
		if (TIMESTAMP == null) {
			readPomProperties();
		}
		return TIMESTAMP;
	}

	/**
	 * This method will read the pom properties file where the version and the build timestamp are and make them available to the web pages via the static class
	 * properties of the same name.
	 */
	public static void readPomProperties() {
		InputStream inputStream = null;
		String pomPropertiesFile = "META-INF/maven/ikube/ikube-core/pom.properties";
		try {
			ClassPathResource classPathResource = new ClassPathResource(pomPropertiesFile, VersionUtilities.class.getClassLoader());
			inputStream = classPathResource.getInputStream();

			Properties properties = new Properties();
			properties.load(inputStream);
			VERSION = properties.getProperty("version");
			IOUtils.closeQuietly(inputStream);

			classPathResource = new ClassPathResource(pomPropertiesFile, VersionUtilities.class.getClassLoader());
			inputStream = classPathResource.getInputStream();
			List<String> lines = IOUtils.readLines(inputStream);
			TIMESTAMP = lines.get(1).replaceAll("#", "");
		} catch (IOException e) {
			LOGGER.error("Exception reading the Maven properties for the build : " + pomPropertiesFile, e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
}
