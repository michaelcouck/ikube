package ikube.listener;

import ikube.toolkit.ThreadUtilities;

public class TerminationListener implements IListener {

	@Override
	public void handleNotification(Event event) {
		if (Event.TERMINATE.equals(event.getType())) {
			ThreadUtilities.destroy();
		}
	}

}
