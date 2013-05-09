package ikube.cluster.listener;

import ikube.scheduling.schedule.Event;

import java.io.Serializable;

/**
 * This interface is for any observer that wants to recieve messages from the system. This could include messages that are distributed in
 * the cluster as well. Typically listeners are added to the grid manager and messages are sent through the grid.
 * 
 * @author Michael Couck
 * @since 15.12.12
 * @version 01.00
 */
public interface IListener<T> {

	/**
	 * Simple class to get an event that can be sent to the listeners.
	 * 
	 * @author Michael Couck
	 */
	public static class EventGenerator {

		/**
		 * This method creates and event and sets the properties/state of the event, ready for transport.
		 * 
		 * @param type the type of event
		 * @param timestamp the timestamp
		 * @param object the object that will be transported, must be serializable as this event will typically be fired into the cluster on
		 *        a topic
		 * @param consumed whether the event is consumes. Listeners should check the consumed flag before executing logic, and set it if
		 *        necessary
		 * @return the event to be fired into the cluster
		 */
		public static Event getEvent(final String type, final long timestamp, final Serializable object, final boolean consumed) {
			Event event = new Event();
			event.setType(type);
			event.setTimestamp(timestamp);
			event.setConsumed(consumed);
			event.setObject(object);
			return event;
		}

	}

	void onMessage(final T t);

}
