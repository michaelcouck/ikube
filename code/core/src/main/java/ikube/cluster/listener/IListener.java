package ikube.cluster.listener;

/**
 * @author Michael Couck
 * @since 15.12.12
 * @version 01.00
 */
public interface IListener<T> {

	void onMessage(final T t);

}
