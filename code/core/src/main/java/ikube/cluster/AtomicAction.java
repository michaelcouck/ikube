package ikube.cluster;

import ikube.IConstants;
import ikube.toolkit.Logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;

/**
 * TODO Document me.
 * 
 * @author Michael Couck
 * @since 29.05.11
 * @version 01.00
 */
public abstract class AtomicAction implements IAtomicAction, IConstants {

	private static final Logger LOGGER = Logger.getLogger(AtomicAction.class);

	private static final int MAX_STACK_TRACES_SIZE = 25;
	private static final LinkedList<String> STACK_TRACES = new LinkedList<String>();

	/**
	 * TODO Document me.
	 * 
	 * @param lockName
	 * @param atomicAction
	 * @return
	 */
	public static boolean executeAction(String lockName, IAtomicAction atomicAction) {
		ILock lock = lock(lockName);
		try {
			return atomicAction.execute();
		} finally {
			unlock(lock);
		}
	}

	/**
	 * TODO Document me.
	 * 
	 * @param lockName
	 * @return
	 */
	public static ILock lock(String lockName) {
		try {
			ILock lock = Hazelcast.getLock(lockName);
			boolean acquired = Boolean.FALSE;
			try {
				acquired = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				LOGGER.error("Interrupted acquiring lock for : " + lockName, e);
			}
			if (!acquired) {
				LOGGER.warn(Logging.getString("Failed to acquire lock : ", lockName, lock, STACK_TRACES.getLast()));
				return null;
			}
			LOGGER.info(Logging.getString("Acquired lock : ", lockName, lock));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			new Throwable().printStackTrace(new PrintStream(outputStream));
			STACK_TRACES.addLast(outputStream.toString());
			while (STACK_TRACES.size() > MAX_STACK_TRACES_SIZE) {
				STACK_TRACES.remove();
			}
			return lock;
		} finally {
			// What can we do here if there is an exception?
		}
	}

	/**
	 * This method unlocks the object map in the cluster.
	 * 
	 * @param lock
	 *            the lock to release
	 */
	public static void unlock(ILock lock) {
		try {
			if (lock != null) {
				lock.unlock();
				lock.destroy();
			}
		} catch (Exception e) {
			LOGGER.error("Exception unlocking : " + lock, e);
		}
	}

}
