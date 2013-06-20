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
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

}