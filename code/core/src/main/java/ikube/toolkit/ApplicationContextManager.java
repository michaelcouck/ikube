package ikube.toolkit;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableInternet;
import ikube.model.IndexableTable;
import ikube.model.Url;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Class for accessing the Spring context.
 * 
 * @author Michael Couck
 * @since 29.04.09
 * @version 01.00
 */
public final class ApplicationContextManager implements ApplicationContextAware {

	private static final Logger LOGGER;
	/** The default location of the configuration files is in the ikube folder at the base of the server. */
	private static final String EXTERNAL_SPRING_CONFIGURATION_FILE = "." + IConstants.SEP + IConstants.IKUBE + IConstants.SEP + IConstants.SPRING_XML;

	private static ApplicationContext APPLICATION_CONTEXT;

	static {
		Logging.configure();
		LOGGER = LoggerFactory.getLogger(ApplicationContextManager.class);
		try {
			SerializationUtilities.setTransientFields(//
					ikube.model.File.class, //
					Url.class, //
					IndexableInternet.class, //
					IndexableEmail.class, //
					IndexableFileSystem.class, //
					IndexableColumn.class, //
					IndexableTable.class, //
					IndexContext.class, //
					ArrayList.class);
		} catch (Exception e) {
			LOGGER.error("Exception setting the transient fields : ", e);
		}
	}

	/**
	 * System wide access to the Spring context.
	 * 
	 * @return the Spring application context for the system
	 */
	public static synchronized ApplicationContext getApplicationContext() {
		try {
			if (APPLICATION_CONTEXT == null) {
				File configFile = null;
				Object ikubeConfigurationPathProperty = System.getProperty(IConstants.IKUBE_CONFIGURATION);
				LOGGER.info("Configuration property file : " + ikubeConfigurationPathProperty);
				// First try the configuration property
				if (ikubeConfigurationPathProperty != null) {
					configFile = new File(ikubeConfigurationPathProperty.toString());
				}
				// See if there is a configuration file at the base of where the Jvm was started
				if (configFile == null || !configFile.isFile()) {
					configFile = new File(EXTERNAL_SPRING_CONFIGURATION_FILE);
				}
				if (configFile != null && configFile.isFile()) {
					// From the file system
					String configFilePath = FileUtilities.cleanFilePath(configFile.getAbsolutePath());
					configFilePath = "file:" + configFilePath;
					LOGGER.info("Configuration file path : " + configFilePath);
					APPLICATION_CONTEXT = getApplicationContextFilesystem(configFilePath);
				} else {
					// Now just get the class path configuration as a default
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
	 * @param <T> the type of bean to return
	 * @param klass the class of the bean
	 * @return the bean with the specified class
	 */
	public static synchronized <T> T getBean(final Class<T> klass) {
		try {
			return getApplicationContext().getBean(klass);
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	public static synchronized Map<String, Object> getBeans() {
		try {
			Map<String, Object> beans = new HashMap<String, Object>();
			String[] beanNames = getApplicationContext().getBeanDefinitionNames();
			for (String beanName : beanNames) {
				Object bean = getApplicationContext().getBean(beanName);
				beans.put(beanName, bean);
			}
			return beans;
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Convenience method to get the bean type from the bean name. Note that this method is not type checked and there is a distinct possibility for a class
	 * cast exception.
	 * 
	 * @param name the name of the bean
	 * @return the bean with the specified name
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T> T getBean(final String name) {
		try {
			return (T) getApplicationContext().getBean(name);
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Access to all the beans of a particular type.
	 * 
	 * @param <T> the expected type
	 * @param klass the class of the beans
	 * @return a map of bean names and beans of type T
	 */
	public static synchronized <T> Map<String, T> getBeans(final Class<T> klass) {
		try {
			return getApplicationContext().getBeansOfType(klass);
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Instantiates the application context using all the configuration files in the parameter list.
	 * 
	 * @param configFiles the locations of the configuration files
	 * @return the merged application context for all the configuration files
	 */
	public static synchronized ApplicationContext getApplicationContextFilesystem(final String... configLocations) {
		try {
			if (APPLICATION_CONTEXT == null) {
				LOGGER.info("Loading the application context with configuration : " + Arrays.deepToString(configLocations));
				APPLICATION_CONTEXT = new FileSystemXmlApplicationContext(configLocations);
				((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
				LOGGER.info("Loaded the application context with configuration : " + Arrays.deepToString(configLocations));
			}
			return APPLICATION_CONTEXT;
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Instantiates the application context using all the configuration files in the parameter list.
	 * 
	 * @param configLocation the locations of the configuration files
	 * @return the merged application context for all the configuration files
	 */
	public static synchronized ApplicationContext getApplicationContext(final String... configLocations) {
		try {
			if (APPLICATION_CONTEXT == null) {
				LOGGER.info("Loading the application context with configurations : " + Arrays.deepToString(configLocations));
				APPLICATION_CONTEXT = new ClassPathXmlApplicationContext(configLocations);
				((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
				LOGGER.info("Loaded the application context with configurations : " + Arrays.deepToString(configLocations));
			}
			return APPLICATION_CONTEXT;
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	/**
	 * Closes the application context.
	 */
	public static synchronized void closeApplicationContext() {
		try {
			if (APPLICATION_CONTEXT != null) {
				((AbstractApplicationContext) APPLICATION_CONTEXT).close();
				APPLICATION_CONTEXT = null;
			}
		} finally {
			ApplicationContextManager.class.notifyAll();
		}
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		if (APPLICATION_CONTEXT == null) {
			LOGGER.info("Setting the application context : " + applicationContext + ", " + applicationContext.getClass());
			ApplicationContextManager.APPLICATION_CONTEXT = applicationContext;
			((AbstractApplicationContext) ApplicationContextManager.APPLICATION_CONTEXT).registerShutdownHook();
		} else {
			LOGGER.info("Application context already loaded : " + APPLICATION_CONTEXT);
		}
	}

}