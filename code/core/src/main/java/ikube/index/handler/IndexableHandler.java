package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

/**
 * Base class for the handlers that contains access to common functionality like the threads etc.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class IndexableHandler<T extends Indexable<?>> implements IHandler<T> {

	protected Logger logger = Logger.getLogger(this.getClass());

	/** The number of threads that this handler will spawn. */
	private int threads;
	/** The class that this handler can handle. */
	private Class<T> indexableClass;
	/** The class that we will need to intercept the document adding to the index. */
	private IDocumentInjector documentInjector;

	public int getThreads() {
		return threads;
	}

	public void setThreads(final int threads) {
		this.threads = threads;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getIndexableClass() {
		return indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexableClass(final Class<T> indexableClass) {
		this.indexableClass = indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDocument(final IndexContext indexContext, final Indexable<T> indexable, final Document document)
			throws CorruptIndexException, IOException {
		// indexContext.getIndex().getIndexWriter().addDocument(document);
		documentInjector.addDocument(indexContext, indexable, document);
	}

	@Override
	public void setDocumentInjector(IDocumentInjector documentInjector) {
		this.documentInjector = documentInjector;
	}

}
