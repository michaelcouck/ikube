package ikube.index.handler;

import ikube.index.handler.email.IndexableEmailHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

/**
 * This is the interface for handlers. Handlers handle indexables. Indexables are essentially sources of data, like an {@link URL} object
 * for example. Any type of data source can then be defined simply by creating another indexable type and mapping the handler to it. For
 * example in the case of email, there is an {@link IndexableEmail} indexable and an {@link IndexableEmailHandler} that handles it. All
 * handlers get the index context passed to them. Using the data in the context that is common to the handlers like the {@link IndexWriter}
 * they can perform their logic to extract the data from their indexable and add it to the index.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public interface IHandler<T extends Indexable<?>> {

	/**
	 * This method is access to the type of class that this handler can handle.
	 * 
	 * @return the type of indexable that this handler can handle
	 */
	Class<T> getIndexableClass();

	/**
	 * Sets the type of indexable that this handler can handle.
	 * 
	 * @param indexableClass
	 *            the class that this handler can handle
	 */
	void setIndexableClass(Class<T> indexableClass);

	/**
	 * This method executes the handler logic. The method returns a list of threads(if it is multi-threaded) that the caller must wait for.
	 * Once all the threads are dead then the handler's logic is complete.
	 * 
	 * @param indexContext
	 *            the index context for the index
	 * @param indexable
	 *            the indexable that the handler must handle
	 * @return the list of threads that the caller must wait for
	 * @throws Exception
	 */
	List<Thread> handle(IndexContext indexContext, T indexable) throws Exception;

	/**
	 * This method is to add the document to the index during the processing. Typically this method will be intercepted and other logic
	 * performed like spatial enrichment and monitoring the performance.
	 * 
	 * @param indexContext
	 *            the index context to add the document to
	 * @param indexable
	 *            the indexable that holds the data to add to the index
	 * @param document
	 *            the document that will be added to the index
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	void addDocument(IndexContext indexContext, Indexable<T> indexable, Document document) throws CorruptIndexException, IOException;

}
