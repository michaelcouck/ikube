package ikube;

import ikube.toolkit.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * This is a central class to execute the tools from the command line. It will call the specified tool and pass in the arguments less the
 * first one which is the class to execute.
 *
 * @author Michael Couck
 * @version 01.00
 * @since at least 14-04-2012
 */
public final class Ikube {

    private static final Logger LOGGER;

    static {
        Logging.configure();
        LOGGER = LoggerFactory.getLogger(Ikube.class);
    }

    public static void main(final String[] args) {
        try {
            // First parameter is the class
            Class<?> target = Class.forName(args[0]);
            Method method = target.getDeclaredMethod("main", String[].class);
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            method.invoke(target, new Object[]{newArgs});
        } catch (final Exception e) {
            LOGGER.error("Usage is: [class-to-execute] <parameters...>");
            LOGGER.error("For example to unpack bzip2 files : java -jar ikube-tool-4.2.2-SNAPSHOT.jar " +
                "ikube.data.wiki.WikiDataUnpacker unpack /media/nas/xfs-one/history");
            LOGGER.error(null, e);
        }
    }

}
