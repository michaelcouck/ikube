package ikube.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * This interface just holds the listener manager that will fire the events to the listeners.
 * 
 * @author Michael Couck
 * @since 17.04.10
 * @version 01.00
 */
public final class ListenerManager {

	private static final Logger LOGGER = Logger.getLogger(ListenerManager.class);
	private static final List<IListener> LISTENERS = new ArrayList<IListener>();
	private static final PooledExecutor POOLED_EXECUTER = new PooledExecutor(5);

	/**
	 * Notifies all the listeners for a particular instance of an event.
	 * 
	 * @param event
	 *            the event for distribution
	 */
	private static synchronized void notifyListeners(final Event event) {
		try {
			for (final IListener listener : Collections.synchronizedList(LISTENERS)) {
				try {
					POOLED_EXECUTER.execute(new Runnable() {
						public void run() {
							listener.handleNotification(event);
						}
					});
					if (event.isConsumed()) {
						break;
					}
				} catch (Exception e) {
					LOGGER.error("Exception notifying listener : " + listener, e);
				}
			}
		} finally {
			ListenerManager.class.notifyAll();
		}
	}

	public static void fireEvent(final Event event) {
		try {
			ListenerManager.notifyListeners(event);
		} catch (Exception e) {
			LOGGER.error("Exception firing event : " + event, e);
		}
	}

	public static void fireEvent(final String type, final long timestamp, final Serializable object, final boolean consumed) {
		Event event = new Event();
		event.setType(type);
		event.setTimestamp(timestamp);
		event.setConsumed(consumed);
		event.setObject(object);
		ListenerManager.fireEvent(event);
	}

	/**
	 * @param listener
	 *            the listener to add for notifications of end of action events
	 */
	public static synchronized void addListener(final IListener listener) {
		try {
			if (LISTENERS.add(listener)) {
				LOGGER.info("Added listener : " + listener);
			} else {
				LOGGER.info("Didn't added listener : " + listener);
			}
		} finally {
			ListenerManager.class.notifyAll();
		}
	}

	/**
	 * @param listener
	 *            the listener to remove from the list of listeners
	 */
	public static synchronized void removeListener(final IListener listener) {
		try {
			if (LISTENERS.remove(listener)) {
				LOGGER.info("Removed listener : " + listener);
			} else {
				LOGGER.info("Didn't removed listener : " + listener);
			}
		} finally {
			ListenerManager.class.notifyAll();
		}
	}

	/**
	 * This method removes all the listeners from the manager for a shutdown perhaps.
	 */
	public static synchronized void removeListeners() {
		try {
			LISTENERS.clear();
		} finally {
			ListenerManager.class.notifyAll();
		}
	}

	private ListenerManager() {
	}

}