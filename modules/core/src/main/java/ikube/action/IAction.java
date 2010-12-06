package ikube.action;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IAction<E, F> {

	public F execute(E e) throws Exception;

}
