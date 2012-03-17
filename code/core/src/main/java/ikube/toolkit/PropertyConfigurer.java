package ikube.toolkit;

import ikube.IConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This class will scan the classpath and the file system below where the process was started looking for properties files to load matching
 * the file name set in the file name pattern variable.
 * 
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
@SuppressWarnings("serial")
public class PropertyConfigurer extends Properties {

	private static final Logger LOGGER = Logger.getLogger(PropertyConfigurer.class);

	private Pattern fileNamePattern;

	/**
	 * This method will look through the class path for properties file with the name specified in the file name property. As well as this
	 * it sill look through the file system checking for properties files on the file system and any jars that are on the file system below
	 * the application will also be checked for the properties file name pattern to load into the property map.
	 */
	public void initialize() {
//		try {
//			// First we check our own jar
//			File thisJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//			LOGGER.info("Checking our own jar : " + thisJar);
//			checkJar(thisJar);
//		} catch (URISyntaxException e) {
//			LOGGER.error("Aaai karumbi! Where am I?", e);
//		}
//		try {
//			// First check the classpath, this could take a while of course
//			String classPathString = System.getProperty("java.class.path");
//			StringTokenizer tokenizer = new StringTokenizer(classPathString, ";", Boolean.FALSE);
//			while (tokenizer.hasMoreTokens()) {
//				String jarLocation = tokenizer.nextToken();
//				try {
//					LOGGER.debug("        : Checking location : " + jarLocation);
//					checkJar(new File(jarLocation));
//				} catch (Exception e) {
//					LOGGER.error("Exception checking jar : " + jarLocation, e);
//				}
//			}
//		} catch (Exception e) {
//			LOGGER.error("Exception scanning the classpath : ", e);
//		}
		// Check the file system for properties files
		List<File> propertyFiles = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), fileNamePattern.toString());
		for (File propertyFile : propertyFiles) {
			try {
				LOGGER.info("        : Loading properties from : " + propertyFile);
				this.load(new FileInputStream(propertyFile));
			} catch (Exception e) {
				LOGGER.error("Exception reading property file : " + propertyFile, e);
			}
		}
		// Check the file system for jars that have the properties files
//		List<File> jarFiles = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), ".jar\\Z");
//		for (File jarFile : jarFiles) {
//			try {
//				LOGGER.debug("        : Loading properties from : " + jarFile);
//				checkJar(jarFile);
//			} catch (Exception e) {
//				LOGGER.error("Exception reading jar file : " + jarFile, e);
//			}
//		}
		// If the system property for the configuration has not been set then set it to the dot directory
		if (System.getProperty(IConstants.IKUBE_CONFIGURATION) == null) {
			System.setProperty(IConstants.IKUBE_CONFIGURATION, ".");
		}
	}
	
	@SuppressWarnings("unused")
	private void printProperties() {
		for (Map.Entry<Object, Object> entry : this.entrySet()) {
			LOGGER.info("Entry : " + entry.getKey() + ":" + entry.getValue());
		}
	}

	/**
	 * Checks a single file for properties entry. This file will typically be a jar file.
	 * 
	 * @param file
	 *            the file to check for properties entries
	 */
	protected void checkJar(File file) {
		if (file == null || !file.isFile() || !file.canRead()) {
			return;
		}
		try {
			LOGGER.debug("Checking jar : " + file);
			checkJar(new JarFile(file));
		} catch (Exception e) {
			LOGGER.error("Exception accessing the properties in jar file : " + file, e);
		}
	}

	/**
	 * Checks a jar file for properties entries that match the pattern for properties names.
	 * 
	 * @param jarFile
	 *            the jar file to check for properties files
	 */
	protected void checkJar(JarFile jarFile) {
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			try {
				JarEntry jarEntry = jarEntries.nextElement();
				String entryName = jarEntry.getName();
				if (fileNamePattern != null && fileNamePattern.matcher(entryName).matches()) {
					LOGGER.debug("        : Loading properties from : " + jarEntry);
					InputStream inputStream = jarFile.getInputStream(jarEntry);
					this.load(inputStream);
				}
			} catch (Exception e) {
				LOGGER.error("Exception loading properties file from jar : " + jarFile, e);
			}
		}
	}

	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = Pattern.compile(".*" + fileNamePattern + ".*");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}