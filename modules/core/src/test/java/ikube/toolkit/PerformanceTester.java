package ikube.toolkit;

import org.apache.log4j.Logger;

/**
 * This class is just a convenience class to see what the performance is for a method or set of methods.
 * 
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class PerformanceTester {

	private static Logger LOGGER = Logger.getLogger(PerformanceTester.class);

	/**
	 * This is the interface to implement by clients that want to test the performance on a method.
	 */
	public interface IPerform {
		public boolean log();

		public void execute() throws Exception;
	}

	public static abstract class APerform implements IPerform {
		public void execute() throws Exception {
		}

		public boolean log() {
			return true;
		}
	}

	/**
	 * Executes the perform object a set number of times, prints the duration and iterations per second to the log and returns the number of
	 * iterations per second.
	 * 
	 * @param perform
	 *            the interface that will call the object to be executed
	 * @param type
	 *            the type of object to be executed, typically a string that will be printed to the output
	 * @param iterations
	 *            the number of executions to perform
	 * @return the number of executions per second
	 */
	public static double execute(IPerform perform, String type, double iterations) throws Exception {
		double start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			long executionStart = System.currentTimeMillis();
			perform.execute();
			if (perform.log()) {
				LOGGER.warn("Execution in : " + (System.currentTimeMillis() - executionStart));
			}
		}
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000d;
		double executionsPerSecond = (iterations / duration);
		if (perform.log()) {
			LOGGER.error("Duration : " + duration + ", " + type + " per second : " + executionsPerSecond);
		}
		return executionsPerSecond;
	}

}