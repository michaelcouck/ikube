package ikube.deploy;

import ikube.deploy.action.IAction;
import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public final class Deployer {

    private static final Logger LOGGER;

    static {
        Logging.configure();
        ThreadUtilities.initialize();
        LOGGER = LoggerFactory.getLogger(Deployer.class);
    }

    private static final String DOT_DIRECTORY = ".";
    private static final String CONFIGURATION_FILE = "deployer\\.xml";

    private static ApplicationContext APPLICATION_CONTEXT;

    public static void main(final String[] args) {
        File configurationDirectory = new File(DOT_DIRECTORY);
        String configurationFile = CONFIGURATION_FILE;
        boolean execute = Boolean.TRUE;
        if (args != null) {
            if (args.length > 1) {
                int upDirectories = 0;
                while (args[0].contains("../")) {
                    args[0] = args[0].replace("../", "");
                    upDirectories++;
                }
                if (StringUtils.isEmpty(args[0])) {
                    args[0] = ".";
                }
                LOGGER.info("Args : " + upDirectories + ", " + Arrays.deepToString(args));
                configurationDirectory = new File(args[0]);
                LOGGER.info("Conf dir before : " + configurationDirectory);
                configurationDirectory = FileUtilities.moveUpDirectories(configurationDirectory, upDirectories);
                LOGGER.info("Conf dir after moving : " + configurationDirectory);
                configurationFile = args[1];
            }
            if (args.length >= 3) {
                execute = Boolean.valueOf(args[2]);
            }
        }
        String configurationDirectoryPath = FileUtilities.cleanFilePath(configurationDirectory.getAbsolutePath());
        LOGGER.info("Directory : " + configurationDirectoryPath + ", file : " + configurationFile);
        // Find the configuration file
        File deployerConfiguration = FileUtilities.findFileRecursively(new File(configurationDirectoryPath), configurationFile);
        LOGGER.info("Configuration file : " + deployerConfiguration);
        String deployerConfigurationPath = "file:" + FileUtilities.cleanFilePath(deployerConfiguration.getAbsolutePath());
        LOGGER.info("Configuration file path : " + deployerConfigurationPath);
        APPLICATION_CONTEXT = new FileSystemXmlApplicationContext(deployerConfigurationPath);
        if (execute) {
            List<Future<?>> futures = new ArrayList<>();
            Deployer deployer = APPLICATION_CONTEXT.getBean(Deployer.class);
            for (final Server server : deployer.getServers()) {
                final String name = Long.toString(System.currentTimeMillis());
                if (!deployer.isParallel()) {
                    // Execute one action at a time
                    for (final IAction action : server.getActions()) {
                        try {
                            action.execute(server);
                        } catch (final Exception e) {
                            LOGGER.error(null, e);
                        }
                    }
                } else {
                    // Execute on action at a time, in order for each server,
                    // but execute all the servers at the same time. So we get the
                    // order of the actions correct, and parallel to the servers
                    Future<?> future = ThreadUtilities.submit(name, new Runnable() {
                        public void run() {
                            try {
                                for (final IAction action : server.getActions()) {
                                    try {
                                        action.execute(server);
                                    } catch (final Exception e) {
                                        LOGGER.error(null, e);
                                    }
                                }
                            } finally {
                                ThreadUtilities.destroy(name);
                            }
                        }
                    });
                    futures.add(future);
                }
            }
            ThreadUtilities.waitForFutures(futures, 6000);
        }
        ThreadUtilities.destroy();
        // System.exit(0);
    }

    public static ApplicationContext getApplicationContext() {
        if (APPLICATION_CONTEXT == null) {
            main(new String[]{DOT_DIRECTORY, CONFIGURATION_FILE, "false"});
        }
        return APPLICATION_CONTEXT;
    }

    private boolean parallel;
    private long maxWaitTime = 60;
    private Collection<Server> servers;

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Collection<Server> getServers() {
        return servers;
    }

    public void setServers(Collection<Server> servers) {
        this.servers = servers;
    }

}
