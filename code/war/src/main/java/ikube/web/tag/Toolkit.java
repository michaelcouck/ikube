package ikube.web.tag;

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
            ikube.toolkit.VERSION.readPomProperties();
            VERSION = ikube.toolkit.VERSION.version();
        }
        return VERSION;
    }

    public static String timestamp() {
        if (TIMESTAMP == null) {
            ikube.toolkit.VERSION.readPomProperties();
            TIMESTAMP = ikube.toolkit.VERSION.timestamp();
        }
        return TIMESTAMP;
    }

}