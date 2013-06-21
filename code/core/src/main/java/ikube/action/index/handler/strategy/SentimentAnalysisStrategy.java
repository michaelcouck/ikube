package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;

/**
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
public class SentimentAnalysisStrategy extends AStrategy {

	public SentimentAnalysisStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		// First detect the language, should already be done in the language detection strategy
		// Detect the subject and the object. Separate the constructs of the sentence for further processing
		// Detect the sentiment based on multiple variables, including possibly the emoticons, weighted of course
		// Feed the data back into the classifiers to re-train them
		// Add all the detected data to the index
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

}