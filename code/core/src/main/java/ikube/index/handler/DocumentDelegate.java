package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

/**
 * This class exists only because we want to intercept the adding of documents to monitor the indexing performance. Spring does not allow
 * private methods to be intercepted, and in this case the addDocument method is called on an abstract super class from a sub class and
 * apparently Spring will not intercept that either,ergo this class that is defined in the configuration, has public methods and can be
 * added to the handlers. We could have used AspectJ without Spring but this overhead of the extra configuration and more importantly the
 * Java agent on the classpath was a little too much to bear.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class DocumentDelegate implements IDocumentDelegate {

	@Override
	public <T> void addDocument(final IndexContext indexContext, final Indexable<T> indexable, final Document document)
			throws CorruptIndexException, IOException {
		indexContext.getIndex().getIndexWriter().addDocument(document);
	}

}
