package ikube.action.index.handler;

import ikube.model.IndexContext;

import org.apache.lucene.document.Document;

/**
 * This interface is the fore runner of separating the content providers from the crawlers completely.
 * 
 * @author Michael Couck
 * @since 25.03.13
 * @version 01.00
 */
public interface IResourceHandler<T> {

	/**
	 * This method will process the resource, extracting the data from whatever type of document or resource it is, and adding all the data to the fields in the
	 * document. This could take a column in a table sof rexample.
	 * 
	 * @param indexContext the index context to add the document to the index for
	 * @param indexable the indexable that is being processed currently
	 * @param document the document to be added to the index
	 * @param resource the resource that will have the
	 * @return the document that was fed in as a parameter as a convenience
	 * @throws Exception
	 */
	Document handleResource(final IndexContext<?> indexContext, final T indexable, final Document document, final Object resource) throws Exception;

}
