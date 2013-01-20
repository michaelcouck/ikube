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
	 * This method will be called around strategic methods in the handlers signifying if the resource should be processed or not.
	 * 
	 * @param parameters the parameters that are passed to the handler method, i.e. the join point parameters
	 * @return whether the processing should continue on this resource
	 */
	boolean aroundProcess(final Object... parameters) throws Exception;

}