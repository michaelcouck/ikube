package ikube.cluster.listener;

import ikube.scheduling.schedule.Event;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @since 15.12.12
 * @version 01.00
 */
public interface IListener<T> {

	public static class EventGenerator {

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
