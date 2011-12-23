package ikube.index.handler.webservice;

import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableImdb;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;

public class IndexableImdbHandler extends IndexableHandler<IndexableImdb> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableImdb indexable) throws Exception {
		// TODO Vishal go to the web service and collect the data into a Document, then call the addDocument
		// method to add it to the database
		Document document = new Document();
		// IndexManager.addStringField(indexable.getIdFieldName(), "id", document, indexable.isStored(), indexable.isAnalyzed(),
		// indexable.isVectored());
		addDocument(indexContext, indexable, document);
		return null;
	}

}