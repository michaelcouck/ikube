package ikube.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has operating system functions, like checking if this is the correct os to execute
 * some tests on, as some tests don't work on CentOs for some obscure reason.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 28-03-2014
 */
public final class OsUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsUtilities.class);

    public static boolean isOs(final String osName) {
        return os().contains(osName);
    }

    public static String os() {
        String localOsName = System.getProperty("os.name");
        String localOsVersion = System.getProperty("os.version");
        String localOsArch = System.getProperty("os.arch");
        LOGGER.info("Name of the OS: " + localOsName);
        LOGGER.info("Version of the OS: " + localOsVersion);
        LOGGER.info("Architecture of the OS: " + localOsArch);
        return localOsName.concat(" ").concat(localOsVersion).concat(" ").concat(localOsArch);
    }

}
