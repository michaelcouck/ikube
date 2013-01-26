package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

public class ResourceHandler<T> implements IResourceHandler<T> {

	protected void addDocument(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document) throws Exception {
		IndexWriter[] indexWriters = indexContext.getIndexWriters();
		// Always add the document to the last index writer in the array, this will
		// be the last one to be added in case the size of the index is exceeded
		indexWriters[indexWriters.length - 1].addDocument(document);
	}

	@Override
	public Document handleResource(IndexContext<?> indexContext, T indexable, Document document, Object resource) throws Exception {
		addDocument(indexContext, (Indexable<?>) indexable, document);
		return document;
	}

}
