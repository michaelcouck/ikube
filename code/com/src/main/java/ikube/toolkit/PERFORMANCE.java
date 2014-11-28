package ikube.toolkit;

import org.apache.log4j.Logger;

/**
 * This class is just a convenience class to see what the performance is for a method or set of methods.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-02-2010
 */
public class PERFORMANCE {

    public static class APerform implements IPerform {

        public void execute() throws Throwable {
            // To be implemented by clients
        }

        public boolean log() {
            return false;
        }

    }

    /**
     * This is the interface to implement by clients that want to test the performance on a method.
     */
    interface IPerform {

        boolean log();

        void execute() throws Throwable;

    }

    private static final Logger LOGGER = Logger.getLogger(PERFORMANCE.class);

    /**
     * Executes the perform object a set number of times, prints the duration and iterations per second
     * to the log and returns the number of iterations per second.
     *
     * @param perform    the interface that will call the object to be executed
     * @param type       the type of object to be executed, typically a string that will be printed to the output
     * @param iterations the number of executions to perform
     * @return the number of executions per second
     */
    public static double execute(final IPerform perform, final String type, final double iterations, final boolean memory) {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        double start = System.currentTimeMillis();
        try {
            for (int i = 0; i < iterations; i++) {
                perform.execute();
            }
        } catch (final Throwable e) {
            LOGGER.error("Exception executing the action : " + perform + ", " + type + ", " + iterations, e);
        }
        double end = System.currentTimeMillis();
        double duration = (end - start) / 1000d;
        double executionsPerSecond = (iterations / duration);
        if (perform.log()) {
            LOGGER.error("Duration : " + duration + ", " + type + " per second : " + executionsPerSecond);
            if (memory) {
                printMemory("Free memory", freeMemory, Runtime.getRuntime().freeMemory());
                printMemory("Max memory", maxMemory, Runtime.getRuntime().maxMemory());
                printMemory("Total memory", totalMemory, Runtime.getRuntime().totalMemory());
            }
        }
        return executionsPerSecond;
    }

    private static void printMemory(final String text, final long before, final long after) {
        long meg = 1 * 1000 * 1000;
        LOGGER.error(text + ", before : " + (before / meg) + ", after : " + (after / meg) + ", increase/decrease : " + ((after - before) / meg));
    }

    /**
     * Singularity.
     */
    private PERFORMANCE() {
        // Documented
    }

}