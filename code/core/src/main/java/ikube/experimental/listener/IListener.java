package ikube.experimental.listener;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
public interface IListener<E extends IEvent<?, ?>> {

    void notify(final E event);

}