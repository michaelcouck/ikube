package ikube.cluster;

import ikube.IConstants;
import ikube.toolkit.Logging;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;

public abstract class AtomicAction implements IAtomicAction, IConstants {

	private static final Logger LOGGER = Logger.getLogger(AtomicAction.class);

	public static boolean executeAction(String lockName, IAtomicAction atomicAction) {
		ILock lock = lock(lockName);
		try {
			return atomicAction.execute();
		} finally {
			unlock(lock);
		}
	}

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
				LOGGER.warn(Logging.getString("Failed to acquire lock : ", lockName, lock));
				Thread.dumpStack();
				// AtomicAction.unlock(lock);
				return null;
			}
			LOGGER.warn(Logging.getString("Acquired lock : ", lockName, lock));
			Thread.dumpStack();
			return lock;
		} finally {
			// AtomicAction.class.notifyAll();
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
		} finally {
			// AtomicAction.class.notifyAll();
		}
	}

}
