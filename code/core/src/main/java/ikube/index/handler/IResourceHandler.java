package ikube.index.handler;

import ikube.model.IndexContext;

import org.apache.lucene.document.Document;

public interface IResourceHandler<T> {

	Document handleResource(final IndexContext<?> indexContext, final T indexable, final Document document, final Object resource)
			throws Exception;

}
