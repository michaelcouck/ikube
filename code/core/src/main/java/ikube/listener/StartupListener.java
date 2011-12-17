package ikube.listener;

import ikube.toolkit.ThreadUtilities;

public class StartupListener implements IListener {

	@Override
	public void handleNotification(Event event) {
		if (Event.STARTUP.equals(event.getType())) {
			ThreadUtilities.initialize();
		}
	}

}
