package ikube.action.rule;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public interface IRule<T> {
	
	boolean evaluate(T object);
	
}
