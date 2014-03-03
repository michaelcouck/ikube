package ikube.action.index.handler;

import ikube.action.index.handler.email.IndexableEmailHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;
import org.apache.lucene.index.IndexWriter;

import java.net.URL;
import java.util.concurrent.ForkJoinTask;

/**
 * This is the interface for handlers. Handlers handle indexables. Indexables are essentially sources of data, like an
 * {@link URL} object for example. Any type of data source can then be defined simply by creating another indexable type
 * and mapping the handler to it. For example in the case of email, there is an {@link IndexableEmail} indexable and an
 * {@link IndexableEmailHandler} that handles it. All handlers get the index context passed to them. Using the data in the
 * context that is common to the handlers like the {@link IndexWriter} they can perform their logic to extract the data
 * from their indexable and add it to
 * the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-11-2010
 */
public interface IIndexableHandler<T extends Indexable<?>> {

    /**
     * This method is access to the type of class that this handler can handle.
     *
     * @return the type of indexable that this handler can handle
     */
    Class<T> getIndexableClass();

    /**
     * Sets the type of indexable that this handler can handle.
     *
     * @param indexableClass the class that this handler can handle
     */
    void setIndexableClass(final Class<T> indexableClass);

    /**
     * This method takes advantage of the fork/join logic from the concurrent package. Implementations
     * must return a single fork join task that may or may not have child tasks. The task will then be scheduled
     * to run asynchronously along with any children.
     *
     * @param indexContext the index context for the index
     * @param indexable    the object being processed, i.e. file system etc.
     * @return the task that will be executed asynchronously
     * @throws Exception any exception is bubbled up to the caller, where everything is caught in the caller
     */
    ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final T indexable) throws Exception;

}