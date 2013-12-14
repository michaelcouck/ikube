package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.StringUtilities;

import org.apache.lucene.document.Document;

/**
 * This strategy will just remove all the non alphanumeric characters from the input.
 * 
 * @author Michael Couck
 * @since 03.12.13
 * @version 01.00
 */
public final class LanguageFilterStrategy extends AStrategy {

	public LanguageFilterStrategy() {
		this(null);
	}

	public LanguageFilterStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
		String cleanedContent = cleanContent(content);
		indexable.setContent(cleanedContent);
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	String cleanContent(final String content) {
		return StringUtilities.stripToAlphaNumeric(content);
	}

}