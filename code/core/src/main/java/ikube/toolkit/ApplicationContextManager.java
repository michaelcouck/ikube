package ikube.toolkit;

import ikube.IConstants;
import ikube.logging.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Class for accessing the Spring context.
 * 
 * @author Michael Couck
 * @since 29.04.09
 * @version 01.00
 */
public class ApplicationContextManager {

	private static Logger LOGGER;
	private static ApplicationContext APPLICATION_CONTEXT;

	static {
		Logging.configure();
		LOGGER = Logger.getLogger(ApplicationContextManager.class);
	}

	/**
	 * System wide access to the Spring context.
	 * 
	 * @return the Spring application context for the system
	 */
	public static synchronized ApplicationContext getApplicationContext() {
		try {
			if (APPLICATION_CONTEXT == null) {
				// First see if there is a configuration file at the base of where the Jvm was started
				File configFile = new File("." + IConstants.SEP + IConstants.IKUBE + IConstants.SEP + IConstants.SPRING_XML);
				LOGGER.info("External configuration file : " + configFile + ", " + configFile.getAbsolutePath() + ", " + configFile.exists());
				if (configFile.exists()) {
					APPLICATION_CONTEXT = getApplicationContext(configFile);
				} else {
					APPLICATION_CONTEXT = getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
				}
			}
			return APPLICATION_CONTEXT;
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Convenience method to get the bean type from the class.
	 * 
	 * @param <T>
	 *            the type of bean to return
	 * @param klass
	 *            the class of the bean
	 * @return the bean with the specified class
	 */
	public static synchronized <T> T getBean(Class<T> klass) {
		try {
			return getApplicationContext().getBean(klass);
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Convenience method to get the bean type from the bean name. Note that this method is not type checked and there is a distinct
	 * possibility for a class cast exception.
	 * 
	 * @param name
	 *            the name of the bean
	 * @return the bean with the specified name
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T> T getBean(String name) {
		try {
			return (T) getApplicationContext().getBean(name);
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Access to all the beans of a particular type.
	 * 
	 * @param <T>
	 *            the expected type
	 * @param klass
	 *            the class of the beans
	 * @return a map of bean names and beans of type T
	 */
	public static synchronized <T> Map<String, T> getBeans(Class<T> klass) {
		try {
			return getApplicationContext().getBeansOfType(klass);
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Instantiates the application context using all the configuration files in the parameter list.
	 * 
	 * @param configFiles
	 *            the locations of the configuration files
	 * @return the merged application context for all the configuration files
	 */
	public static synchronized ApplicationContext getApplicationContext(File... configFiles) {
		try {
			if (APPLICATION_CONTEXT == null) {
				LOGGER.info("Loading the application context with configurations : " + Arrays.asList(configFiles));
				List<String> configLocations = new ArrayList<String>();
				for (File configurationFile : configFiles) {
					configLocations.add(configurationFile.getAbsolutePath());
				}
				APPLICATION_CONTEXT = new FileSystemXmlApplicationContext(configLocations.toArray(new String[configLocations.size()]));
				LOGGER.info("Loaded the application context with configurations : " + Arrays.asList(configFiles));
			}
			return APPLICATION_CONTEXT;
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Instantiates the application context using all the configuration files in the parameter list.
	 * 
	 * @param configLocations
	 *            the locations of the configuration files
	 * @return the merged application context for all the configuration files
	 */
	public static synchronized ApplicationContext getApplicationContext(String... configLocations) {
		try {
			if (APPLICATION_CONTEXT == null) {
				LOGGER.info("Loading the application context with configurations : " + Arrays.asList(configLocations));
				APPLICATION_CONTEXT = new ClassPathXmlApplicationContext(configLocations);
				LOGGER.info("Loaded the application context with configurations : " + Arrays.asList(configLocations));
			}
			return APPLICATION_CONTEXT;
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

}