package ikube.action.index.handler;

import ikube.model.IndexContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class for the resource providers, common logic in other words.
 *
 * @author Michael Couck
 * @version 01.00
 * @see {@link IResourceHandler}
 * @since 25-03-2013
 */
public class ResourceHandler<T> implements IResourceHandler<T> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(
            final IndexContext indexContext,
            final T indexable,
            final Document document,
            final Object resource)
            throws Exception {
        addDocument(indexContext, document);
        return document;
    }

    /**
     * Adds the document to the index writer in the index context.
     *
     * @param indexContext the index context to add the document to the index for
     * @param document     the document to be added to the index
     * @throws Exception
     */
    protected void addDocument(
            final IndexContext indexContext,
            final Document document)
            throws Exception {
        IndexWriter[] indexWriters = indexContext.getIndexWriters();
        // Always add the document to the last index writer in the array, this will
        // be the last one to be added in case the size of the index is exceeded
        indexWriters[indexWriters.length - 1].addDocument(document);
    }

}
