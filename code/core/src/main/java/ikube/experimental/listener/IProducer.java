package ikube.experimental.listener;

/**
 * Tagging interface for components that produce events and publish them to the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-08-2015
 */
public interface IProducer<E extends IEvent<?, ?>> {

    /**
     * Fires the event in the grid.
     *
     * @param event the event to publish to the grid
     */
    void fire(final E event);

}