package ikube.index.handler;

/**
 * This interface is for pre and post processing documents during indexing. Chains of these classes will aggregate a response, and based on
 * whether true or false, the handler can then take action or not according to the pre-requisites.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public interface IStrategy /* extends Serializable */{

	/**
	 * This method will perform pre-processing logic, and return a true or false result, defining whether the caller should proceed with
	 * it's logic or not. This response can be ignored of course, or taken as an advice.
	 * 
	 * @param parameters the parameters that this strategy needs to perform it's logic. The caller then needs to know this
	 * @return whether the logic of this strategy indicates that the processing of this document should continue
	 */
	boolean preProcess(final Object... parameters);

	/**
	 * This method will post process the resource, and potentially return a false result indicating to the caller that the resource was not
	 * suitable. The caller can choose to ignore this result.
	 * 
	 * @param parameters the parameters that this strategy needs to perform it's logic. The caller then needs to know this
	 * @return whether the logic of this strategy indicates that the processing of this document should continue
	 */
	boolean postProcess(final Object... parameters);

}