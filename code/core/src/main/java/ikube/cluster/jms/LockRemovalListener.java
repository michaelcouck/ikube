package ikube.cluster.jms;

import ikube.cluster.IClusterManager;
import ikube.cluster.jms.ClusterManagerJms.Lock;
import ikube.listener.Event;
import ikube.listener.IListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LockRemovalListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LockRemovalListener.class);

	/** The time out time for the lock in case a server dies. */
	private static final long LOCK_TIME_OUT = 60000;

	@Autowired
	private IClusterManager clusterManager;

	@Override
	public void handleNotification(Event event) {
		if (!Event.LOCK_RELEASE.equals(event.getType())) {
			return;
		}
		try {
			List<String> toRemove = new ArrayList<String>();
			for (Entry<String, Lock> entry : clusterManager.getLocks().entrySet()) {
				if (System.currentTimeMillis() - entry.getValue().shout > LOCK_TIME_OUT) {
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
