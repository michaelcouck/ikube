package ikube.index.handler;

/**
 * This interface is for pre and post processing documents during indexing. Chains of these classes will aggregate a response, and based on
 * whether true or false, the handler can then take action or not according to the pre-requisites.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public interface IStrategy<T, U> {

	/**
	 * This method will perform pre-processing logic, and return a true or false result, defining whether the caller should proceed with
	 * it's logic or not. This response can be ignored of course, or taken as an advice.
	 * 
	 * @param t the context object
	 * @param u the object that will be inspected and or processed
	 * @return whether the logic of this strategy indicates that the processing of this document should continue
	 */
	boolean preProcess(T t, U u);

	/**
	 * This method will post process the resource, and potentially return a false result indicating to the caller that the resource was not
	 * suitable. The caller can choose to ignore this result.
	 * 
	 * @param t the context object
	 * @param u the object that will be inspected and or processed
	 * @return whether the logic of this strategy indicates that the processing of this document should continue
	 */
	boolean postProcess(T t, U u);

}