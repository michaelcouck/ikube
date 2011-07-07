package ikube.cluster;

import ikube.IConstants;
import ikube.toolkit.Logging;

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

	/**
	 * TODO Document me.
	 * 
	 * @param lockName
	 * @param atomicAction
	 * @return
	 */
	public static synchronized Object executeAction(String lockName, IAtomicAction atomicAction) {
		ILock lock = AtomicAction.lock(lockName);
		try {
			if (lock == null) {
				return null;
			}
			return atomicAction.execute();
		} finally {
			AtomicAction.unlock(lock);
			AtomicAction.class.notifyAll();
		}
	}

	/**
	 * TODO Document me.
	 * 
	 * @param lockName
	 * @return
	 */
	public static ILock lock(String lockName) {
		ILock lock = null;
		try {
			lock = Hazelcast.getLock(lockName);
			boolean acquired = Boolean.FALSE;
			try {
				acquired = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				LOGGER.error("Interrupted acquiring lock for : " + lockName, e);
			}
			if (!acquired) {
				LOGGER.warn(Logging.getString("Failed to acquire lock : ", lockName, lock));
				unlock(lock);
				return null;
			}
			LOGGER.debug(Logging.getString("Acquired lock : ", lockName, lock));
		} catch (Exception e) {
			LOGGER.error("Exception trying to lock : " + lockName + ", will unlock if possible : ", e);
			unlock(lock);
		}
		return lock;
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