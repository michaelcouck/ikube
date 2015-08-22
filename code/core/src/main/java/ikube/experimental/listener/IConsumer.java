package ikube.experimental.listener;

/**
 * Tagging interface for components that consume events from the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
public interface IConsumer<E extends IEvent<?, ?>> {

    /**
     * Notification of a grid event. The event is not consumed per se, it goes on
     * to notify other consumers of the type of event defined for the consumer.
     *
     * @param event the event from the grid
     */
    void notify(final E event);

}