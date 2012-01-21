package ikube.toolkit;

import ikube.IConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * TODO Add support to this class to load the default core application configuration from the jar and other client configuration from the
 * Ikube folder out side the container.
 * 
 * Class for accessing the Spring context.
 * 
 * @author Michael Couck
 * @since 29.04.09
 * @version 01.00
 */
public final class ApplicationContextManager implements ApplicationContextAware {

	private static final Logger LOGGER;
	private static final String EXTERNAL_SPRING_CONFIGURATION_FILE = "." + IConstants.SEP + IConstants.IKUBE + IConstants.SEP
			+ IConstants.SPRING_XML;

	private static ApplicationContext APPLICATION_CONTEXT;

	static {
		Logging.configure();
		LOGGER = Logger.getLogger(ApplicationContextManager.class);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				ThreadUtilities.destroy();
			}
		});
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
				File configFile = new File(EXTERNAL_SPRING_CONFIGURATION_FILE);
				if (configFile.exists()) {
					LOGGER.info("External configuration file : " + configFile + ", " + configFile.getAbsolutePath() + ", "
							+ configFile.exists());
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
	 * Convenience method to get the bean type from the bean name. Note that this method is not type checked and there is a distinct
	 * possibility for a class cast exception.
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
	public static synchronized ApplicationContext getApplicationContext(final File... configFiles) {
		try {
			if (APPLICATION_CONTEXT == null) {
				LOGGER.info("Loading the application context with configurations : " + Arrays.asList(configFiles));
				List<String> configLocations = new ArrayList<String>();
				for (File configurationFile : configFiles) {
					configLocations.add(configurationFile.getAbsolutePath());
				}
				APPLICATION_CONTEXT = new FileSystemXmlApplicationContext(configLocations.toArray(new String[configLocations.size()]));
				((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
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
	 * @param configLocations the locations of the configuration files
	 * @return the merged application context for all the configuration files
	 */
	public static synchronized ApplicationContext getApplicationContext(final String... configLocations) {
		try {
			if (APPLICATION_CONTEXT == null) {
				LOGGER.info("Loading the application context with configurations : " + Arrays.asList(configLocations));
				APPLICATION_CONTEXT = new ClassPathXmlApplicationContext(configLocations);
				((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
				LOGGER.info("Loaded the application context with configurations : " + Arrays.asList(configLocations));
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
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (APPLICATION_CONTEXT == null) {
			LOGGER.info("Setting the application context : " + applicationContext);
			ApplicationContextManager.APPLICATION_CONTEXT = applicationContext;
			((AbstractApplicationContext) ApplicationContextManager.APPLICATION_CONTEXT).registerShutdownHook();
		} else {
			LOGGER.info("Application context already loaded : " + APPLICATION_CONTEXT);
		}
	}

	/**
	 * TODO Implement this class completely.
	 * 
	 * This class is to be able to mix and match the configuration from the file system and the classpath. Essentially what we want to do is
	 * to have the default and application configuration in the core jar and the client configuration outside the container in the Ikube
	 * folder.
	 * 
	 * @author Michael Couck
	 * @since 07.06.11
	 * @version 01.00
	 */
	public static class RelativeXmlApplicationContext extends ClassPathXmlApplicationContext {

		public RelativeXmlApplicationContext(String... configLocations) {
			super(configLocations);
		}

		@Override
		public Resource[] getResources(String locationPattern) throws IOException {
			// LOGGER.info("Location pattern : " + locationPattern);
			return super.getResources(locationPattern);
		}

		@Override
		public Resource getResource(String location) {
			// LOGGER.info("Location : " + location);
			return super.getResource(location);
		}

		@Override
		protected Resource getResourceByPath(String path) {
			// LOGGER.info("Path : " + path);
			return super.getResourceByPath(path);
		}

	}
	
	public static void main(String[] args) {
		File file = FileUtilities.findFileRecursively(new File("."), "Ikube.html");
		String content = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
		System.out.println(content);
		
		System.out.println("  _   _           _");
		System.out.println(" |_| | |         | |      ");
		System.out.println("      | |         | |     ");
	}

}