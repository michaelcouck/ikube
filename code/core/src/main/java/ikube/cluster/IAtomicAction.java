package ikube.cluster;

/**
 * TODO Doment me!
 * 
 * @author Michael Couck
 * @since 20.02.11
 * @version 01.00
 */
public interface IAtomicAction {
	
	/**
	 * TODO Doment me!
	 * 
	 * @param <T>
	 * @return
	 */
	<T> T execute();
	
}
