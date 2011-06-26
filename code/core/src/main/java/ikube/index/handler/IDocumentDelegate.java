package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

public interface IDocumentDelegate {

	<T> void addDocument(final IndexContext<?> indexContext, final Indexable<T> indexable, final Document document)
			throws CorruptIndexException, IOException;

}
