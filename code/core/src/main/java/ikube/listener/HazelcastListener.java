package ikube.listener;

/**
 * This class checks that Hazelcast is still active in the cluster, i.e. not fallen down and restarts the instance if not.
 * 
 * @author Michael couck
 * @since 10.10.12
 * @version 01.00
 */
public class HazelcastListener implements IListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNotification(Event event) {
		if (!Event.TIMER.equals(event.getType())) {
			return;
		}
		// TODO Find out if this instance is still active some how and
		// if not then close all the related objects and restart the instance
	}

}
