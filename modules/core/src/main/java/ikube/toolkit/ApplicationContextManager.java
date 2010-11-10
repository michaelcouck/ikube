package ikube.toolkit;

import ikube.IConstants;
import ikube.logging.Logging;

import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



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
		try {
			Logging.configure();
			LOGGER = Logger.getLogger(ApplicationContextManager.class);
		} catch (Exception e) {
			LOGGER.error("Exception starting the logging and Bitronix configuration", e);
		}
	}

	/**
	 * System wide access to the Spring context.
	 *
	 * @return the Spring application context for the system
	 */
	public static synchronized ApplicationContext getApplicationContext() {
		if (APPLICATION_CONTEXT == null) {
			try {
				APPLICATION_CONTEXT = getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
			} catch (Exception e) {
				LOGGER.error("Exception initilizing the application context for Spring", e);
			}
		}
		return APPLICATION_CONTEXT;
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
		return getApplicationContext().getBean(klass);
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
		return (T) getApplicationContext().getBean(name);
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
		return getApplicationContext().getBeansOfType(klass);
	}

	/**
	 * Instantiates the application context using all the configuration files in the parameter list.
	 *
	 * @param configLocations
	 *            the locations of the configuration files
	 * @return the merged application context for all the configuration files
	 */
	public static synchronized ApplicationContext getApplicationContext(String... configLocations) {
		if (APPLICATION_CONTEXT == null) {
			LOGGER.info("Loading the application context with configurations : " + Arrays.asList(configLocations));
			APPLICATION_CONTEXT = new ClassPathXmlApplicationContext(configLocations, ApplicationContextManager.class);
		}
		return APPLICATION_CONTEXT;
	}

}