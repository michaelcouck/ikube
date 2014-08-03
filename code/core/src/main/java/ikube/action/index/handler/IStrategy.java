package ikube.action.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;

/**
 * This interface is for pre and post processing documents during indexing. Chains of these classes will aggregate a response, and based on whether true or
 * false, the handler can then take action or not according to the pre-requisites.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public interface IStrategy {

	/**
	 * This method will be called around strategic methods in the handlers signifying if the resource should be processed or not.
	 * 
	 * @param indexContext the index context that is being indexed
	 * @param indexable the indexable that is being processed
	 * @param document the document for the Lucene index where the processed data will be stored presumably, but not necessarily
	 * @param resource the resource that is to be processed and post processed of course
	 * @return whether the processing should continue on this resource
	 */
	boolean preProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception;

	/**
	 * This method will post process the data, interesting for enriching the data after the fact.
	 * 
	 * @param indexContext the index context that is being indexed
	 * @param indexable the indexable that is being processed
	 * @param document the document for the Lucene index where the processed data will be stored presumably, but not necessarily
	 * @param resource the resource that is to be processed and post processed of course
	 * @return whether the processing should continue on this resource
	 */
	boolean postProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception;

}