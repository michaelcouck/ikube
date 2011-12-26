package ikube.listener;

import ikube.toolkit.ThreadUtilities;

/**
 * This listener is to terminate the executer service, essentially aborting any actions that may be submitted, like indexing for example.
 * 
 * @author Michael Couck
 * @since 24.12.11
 * @version 01.00
 */
public class TerminationListener implements IListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNotification(Event event) {
		if (Event.TERMINATE.equals(event.getType())) {
			ThreadUtilities.destroy();
		}
	}

}
