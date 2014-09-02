package ikube.web.tag;

import ikube.toolkit.VersionUtilities;

/**
 * A set of functions that can be used in Jsp pages, like concatenation of strings and getting the size of collections.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-12-2011
 */
public class Toolkit {

    private static String VERSION;
    private static String TIMESTAMP;

    public static String version() {
        if (VERSION == null) {
            VersionUtilities.readPomProperties();
            VERSION = VersionUtilities.version();
        }
        return VERSION;
    }

    public static String timestamp() {
        if (TIMESTAMP == null) {
            VersionUtilities.readPomProperties();
            TIMESTAMP = VersionUtilities.timestamp();
        }
        return TIMESTAMP;
    }

}