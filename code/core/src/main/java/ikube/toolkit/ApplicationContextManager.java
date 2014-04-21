package ikube.toolkit;

import ikube.IConstants;
import ikube.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class for accessing the Spring context.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-04-2009
 */
public final class ApplicationContextManager implements ApplicationContextAware {

    private static final Logger LOGGER;
    /**
     * The default location of the configuration files is in the ikube folder at the base of the server.
     */
    private static final String EXTERNAL_SPRING_CONFIGURATION_FILE =
            "." +
                    IConstants.SEP +
                    IConstants.IKUBE +
                    IConstants.SEP +
                    IConstants.SPRING_XML;

    private static ApplicationContext APPLICATION_CONTEXT;

    static {
        Logging.configure();
        LOGGER = LoggerFactory.getLogger(ApplicationContextManager.class);
        try {
            SerializationUtilities.setTransientFields(//
                    ikube.model.File.class, //
                    Url.class, //
                    Analysis.class, //
                    Search.class, //
                    Task.class, //
                    IndexableInternet.class, //
                    IndexableEmail.class, //
                    IndexableFileSystem.class, //
                    IndexableColumn.class, //
                    IndexableTable.class, //
                    IndexContext.class, //
                    ArrayList.class);
        } catch (final Exception e) {
            LOGGER.error("Exception setting the transient fields : ", e);
        }
    }

    /**
     * System wide access to the Spring context. This method is called when Ikube is started without a server
     * i.e. in stand alone mode, or from the integration tests. Generally it will be in a server and the Spring
     * web context will handle the initialization.
     *
     * @return the Spring application context for the system
     */
    public static synchronized ApplicationContext getApplicationContext() {
        try {
            if (APPLICATION_CONTEXT == null) {
                String configFilePath = getConfigiFilePath();
                if (configFilePath != null) {
                    APPLICATION_CONTEXT = getApplicationContextFilesystem(configFilePath);
                } else {
                    // Now just get the class path configuration as a default
                    LOGGER.info("Default location for configuration file : ");
                    APPLICATION_CONTEXT = getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
                }
            }
            return APPLICATION_CONTEXT;
        } finally {
            ApplicationContextManager.class.notifyAll();
        }
    }

    private static String getConfigiFilePath() {
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
        if (configFile.isFile()) {
            // From the file system
            String configFilePath = FileUtilities.cleanFilePath(configFile.getAbsolutePath());
            configFilePath = "file:" + configFilePath;
            LOGGER.info("Configuration file path : " + configFilePath);
            return configFilePath;
        }
        return null;
    }

    /**
     * Convenience method to get the bean type from the class.
     *
     * @param <T>   the type of bean to return
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

    /**
     * Convenience method to get the bean type from the bean name. Note that this method is not
     * type checked and there is a distinct possibility for a class cast exception.
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
     * @param <T>   the expected type
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
     * This method will register a bean dynamically. There are no properties set in the bean and
     * the default constructor will probably be used by Spring.
     *
     * @param name          the name of the bean, unique in the application context, i.e. the id
     * @param beanClassName the class type of the bean to create
     * @return the newly created bean, as a singleton, for convenience
     */
    public static synchronized <T> T setBean(final String name, final String beanClassName) {
        ApplicationContext applicationContext = getApplicationContext();
        if (AbstractRefreshableApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
            BeanFactory beanFactory = ((AbstractRefreshableApplicationContext) applicationContext).getBeanFactory();
            if (DefaultListableBeanFactory.class.isAssignableFrom(beanFactory.getClass())) {
                BeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClassName(beanClassName);
                ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition(name, beanDefinition);
            }
        }
        return getBean(name);
    }

    /**
     * Instantiates the application context using all the configuration files in the parameter list.
     *
     * @param configLocations the locations of the configuration files
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
     * @param configLocations the locations of the configuration files
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

    /**
     * This method is called by the 'web' part of the Spring configuration, which sets the context for us.
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        if (APPLICATION_CONTEXT == null) {
            LOGGER.info("Setting the application context from the web part : " + applicationContext + ", " + applicationContext.getClass());
            APPLICATION_CONTEXT = applicationContext;
            ((AbstractApplicationContext) APPLICATION_CONTEXT).registerShutdownHook();
        } else {
            LOGGER.info("Application context already loaded : " + APPLICATION_CONTEXT);
        }
    }

    /**
     * This class w<ill check if there are any changes in the configuration files
     * and if so then refresh(restart) the application context. At the time of writing(15-03-2014)
     * this was untested.
     */
    class ApplicationContextRefresher implements Runnable {

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            List<File> configurationFiles = new ArrayList<>();
            long sleep = IConstants.SIXTY_SECONDS;
            do {
                ThreadUtilities.sleep(sleep);
                try {
                    if (APPLICATION_CONTEXT == null) {
                        continue;
                    }
                    List<File> newConfigurationFiles = new ArrayList<>();
                    if (AbstractRefreshableWebApplicationContext.class.isAssignableFrom(APPLICATION_CONTEXT.getClass())) {
                        String[] configLocations = ((AbstractRefreshableWebApplicationContext) APPLICATION_CONTEXT).getConfigLocations();
                        for (final String configLocation : configLocations) {
                            newConfigurationFiles.add(new File(configLocation));
                        }
                    } else {
                        File configurationFile = new File(getConfigiFilePath());
                        if (configurationFile.exists() && configurationFile.isFile() && configurationFile.canRead()) {
                            List<File> springFiles = FileUtilities.findFilesRecursively(configurationFile.getParentFile(), new ArrayList<File>(), "spring.*\\.xml");
                            List<File> propertiesFiles = FileUtilities.findFilesRecursively(configurationFile.getParentFile(), new ArrayList<File>(), "spring\\.properties");
                            newConfigurationFiles.addAll(springFiles);
                            newConfigurationFiles.addAll(propertiesFiles);
                        }
                    }
                    boolean mustRefresh = Boolean.FALSE;
                    if (configurationFiles.isEmpty()) {
                        LOGGER.info("Initializing the files : ");
                    } else if (configurationFiles.size() != newConfigurationFiles.size()) {
                        // Refresh the application context
                        LOGGER.info("Should refresh the application context : ");
                        mustRefresh = Boolean.TRUE;
                    } else {
                        for (int i = 0; i < configurationFiles.size(); i++) {
                            File one = configurationFiles.get(i);
                            File two = newConfigurationFiles.get(i);
                            if (one.lastModified() != two.lastModified()) {
                                LOGGER.info("Should refresh the application context : ");
                                mustRefresh = Boolean.TRUE;
                                break;
                            }
                        }
                    }
                    // LOGGER.debug("Must refresh : " + mustRefresh);
                    configurationFiles.clear();
                    configurationFiles.addAll(newConfigurationFiles);
                    if (mustRefresh) {
                        if (AbstractRefreshableWebApplicationContext.class.isAssignableFrom(APPLICATION_CONTEXT.getClass())) {
                            LOGGER.info("Refreshing application context : " + APPLICATION_CONTEXT);
                            ((AbstractRefreshableWebApplicationContext) APPLICATION_CONTEXT).refresh();
                        } else {
                            LOGGER.info("Can't refresh application context : " + APPLICATION_CONTEXT.getClass().getName());
                        }
                    }
                } catch (final Exception e) {
                    // If we have an exception refreshing then sleep for longer
                    sleep *= 2;
                    LOGGER.error("Exception refreshing the application context : ", e);
                }
            } while (true);
        }

    }

    public void initialize() {
        ThreadUtilities.submit(IConstants.APPLICATION_CONTEXT_REFRESHER, new ApplicationContextRefresher());
    }

}