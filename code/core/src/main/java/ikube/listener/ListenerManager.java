package ikube.listener;

import java.io.Serializable;
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

	private static final Logger LOGGER = Logger.getLogger(ListenerManager.class);

	private List<IListener> listeners;

	public ListenerManager() {
		listeners = new ArrayList<IListener>();
	}

	public void fireEvent(final Event event) {
		try {
			notifyListeners(event);
		} catch (Exception e) {
			LOGGER.error("Exception firing event : " + event, e);
		}
	}

	public void fireEvent(final String type, final long timestamp, final Serializable object, final boolean consumed) {
		Event event = getEvent(type, timestamp, object, consumed);
		fireEvent(event);
	}

	public static Event getEvent(final String type, final long timestamp, final Serializable object, final boolean consumed) {
		Event event = new Event();
		event.setType(type);
		event.setTimestamp(timestamp);
		event.setConsumed(consumed);
		event.setObject(object);
		return event;
	}

	/**
	 * @param listener
	 *            the listener to add for notifications of end of action events
	 */
	public synchronized void addListener(final IListener listener) {
		try {
			if (listeners.add(listener)) {
				LOGGER.info("Added listener : " + listener);
			} else {
				LOGGER.info("Didn't added listener : " + listener);
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method removes all the listeners from the manager for a shutdown perhaps.
	 */
	public synchronized void removeListeners() {
		try {
			listeners.clear();
		} finally {
			notifyAll();
		}
	}

	/**
	 * Notifies all the listeners for a particular instance of an event.
	 * 
	 * @param event
	 *            the event for distribution
	 */
	private void notifyListeners(final Event event) {
		for (final IListener listener : listeners) {
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

	public void setListeners(List<IListener> listeners) {
		this.listeners = listeners;
	}

}