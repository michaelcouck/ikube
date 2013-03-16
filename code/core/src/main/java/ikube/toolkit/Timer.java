package ikube.toolkit;

/**
 * This is just a utility that will return t he time taken for a particular piece of logic.
 * 
 * @author Michael Couck
 * @since 10.03.13
 * @version 01.00
 */
public final class Timer {

	/**
	 * Implement this interface and put the logic to be timed in the execute method and feed it to the {@link Timer#execute(Timed)} method,
	 * the return value will be the time taken in milliseconds for the logic to be executed.
	 * 
	 * @author Michael Couck
	 * @since 10.03.13
	 * @version 01.00
	 */
	public interface Timed {
		void execute();
	}

	/**
	 * This method takes a timed object and executes the logic to be times, returning the time taken for the execution to finish in
	 * milliseconds.
	 * 
	 * @param timed the wrapper for the logic that is to be timed
	 * @return the time taken for the logic to execute in milliseconds
	 */
	public static long execute(final Timed timed) {
		long start = System.currentTimeMillis();
		timed.execute();
		return System.currentTimeMillis() - start;
	}

}
