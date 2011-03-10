package ikube.listener;


/**
 * Listener interface for system events related to the actions. Actions generate events during their life cycle, listeners may register to
 * receive these events.
 *
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
public interface IListener {

	/**
	 * This method is called on each of the listeners for all events. It is up to each individual listener to then decide what to do with
	 * that particular type of event. This method must return promptly because there is no stack in the listener manager, i.e. all the other
	 * listeners will be waiting for this manager to notify them too. So if any implementors plan on taking a long time executing their
	 * logic with the event then they should handle this by starting in a new thread.
	 *
	 * @param event
	 *            the event that was fired by a component
	 */
	void handleNotification(Event event);

}