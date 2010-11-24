package ikube.listener;

import ikube.model.Event;
import ikube.model.IndexContext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This interface just holds the listener manager that will fire the events to the listeners.
 *
 * @author Michael Couck
 * @since 17.04.10
 * @version 01.00
 */
public class ListenerManager {

	private static Logger LOGGER = Logger.getLogger(ListenerManager.class);
	private static List<IListener> LISTENERS = new ArrayList<IListener>();

	/**
	 * @param listener
	 *            the listener to add for notifications of end of action events
	 */
	public static void addListener(IListener listener) {
		if (LISTENERS.add(listener)) {
			LOGGER.info("Added listener : " + listener);
		} else {
			LOGGER.info("Didn't added listener : " + listener);
		}
	}

	/**
	 * @param listener
	 *            the listener to remove from the list of listeners
	 */
	public static void removeListener(IListener listener) {
		if (LISTENERS.remove(listener)) {
			LOGGER.info("Removed listener : " + listener);
		} else {
			LOGGER.info("Didn't removed listener : " + listener);
		}
	}

	/**
	 * Notifies all the listeners for a particular instance of an event.
	 *
	 * @param event
	 *            the event for distribution
	 */
	private static final void notifyListeners(final Event event) {
		for (final IListener listener : LISTENERS) {
			try {
				listener.handleNotification(event);
				if (event.isConsumed()) {
					break;
				}
			} catch (Exception e) {
				LOGGER.error("Exception notifying listener : " + listener, e);
			}
		}
	}

	public static final void fireEvent(Event event) {
		try {
			ListenerManager.notifyListeners(event);
		} catch (Exception e) {
			LOGGER.error("Exception firing event : " + event, e);
		}
	}

	public static final void fireEvent(IndexContext indexContext, String type, Timestamp timestamp, boolean consumed) {
		Event event = new Event();
		event.setIndexContext(indexContext);
		event.setType(type);
		event.setTimestamp(timestamp);
		event.setConsumed(consumed);
		ListenerManager.fireEvent(event);
	}

}