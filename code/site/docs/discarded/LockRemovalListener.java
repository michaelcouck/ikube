package ikube.cluster.jms;

import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class will get triggered by the scheduler. When a server wants to execute an action, it has to send a lock message to the cluster
 * that the execution of the rules happens atomically. However, if the server gains the lock and then dies before releasing it, the cluster
 * will stay locked. This class will remove locks that have times out. Typically a lock is only held for a few seconds before being
 * released.
 * 
 * @author Michael Couck
 * @since 31.12.11
 * @version 01.00
 */
public class LockRemovalListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LockRemovalListener.class);

	/** The time out time for the lock in case a server dies. */
	private static final long LOCK_TIME_OUT = 60000;

	/** The access to the locks for this server. */
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNotification(Event event) {
		if (!Event.LOCK_RELEASE.equals(event.getType())) {
			return;
		}
		try {
			List<String> toRemove = new ArrayList<String>();
			for (Entry<String, ClusterManagerJmsLock> entry : clusterManager.getLocks().entrySet()) {
				boolean locked = entry.getValue().isLocked();
				boolean timedOut = System.currentTimeMillis() - entry.getValue().getShout() > LOCK_TIME_OUT;
				if (locked && timedOut) {
					toRemove.add(entry.getKey());
				}
			}
			for (String lockKey : toRemove) {
				LOGGER.warn("Removing lock from time out : " + clusterManager.getLocks().get(lockKey));
				clusterManager.getLocks().remove(lockKey);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in the lock timeout thread : ", e);
		}
	}
}
