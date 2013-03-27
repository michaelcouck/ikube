package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * This is the base class for the resource providers, common logic in other words.
 * 
 * @see {@link IResourceHandler}
 * @author Michael Couck
 * @since 25.03.13
 * @version 01.00
 */
public class ResourceHandler<T> implements IResourceHandler<T> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document handleResource(IndexContext<?> indexContext, T indexable, Document document, Object resource) throws Exception {
		addDocument(indexContext, (Indexable<?>) indexable, document);
		return document;
	}

	/**
	 * Adds the document to the index writer in the index context.
	 * 
	 * @param indexContext the index context to add the document to the index for
	 * @param indexable the indexable that is being processed currently
	 * @param document the document to be added to the index
	 * @throws Exception
	 */
	protected void addDocument(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document) throws Exception {
		IndexWriter[] indexWriters = indexContext.getIndexWriters();
		// Always add the document to the last index writer in the array, this will
		// be the last one to be added in case the size of the index is exceeded
		indexWriters[indexWriters.length - 1].addDocument(document);
	}

}
