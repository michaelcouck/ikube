package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;

/**
 * @author Michael Couck
 * @since 01.07.13
 * @version 01.00
 */
public final class LanguageCleaningStrategy extends AStrategy {

	public LanguageCleaningStrategy() {
		this(null);
	}

	public LanguageCleaningStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
		if (content != null) {
			StringBuilder stringBuilder = new StringBuilder();
			// Remove multiple characters, like aaaaahhhhhyyyeeeah, to aahhyyeeah
			char[] chars = content.toCharArray();
			for (int i = 0, j = 1; i < chars.length; i++, j++) {
				char c = chars[i];
				if (i != 0 && c == chars[i - 1] && chars.length >= j && c == chars[j]) {
					continue;
				}
				stringBuilder.append(c);
			}
			// Correct any obvious spelling mistakes
			String cleanedContent = stringBuilder.toString().toLowerCase();
			// logger.info("Cleaned : " + cleanedContent);
			// TODO Do we really want to do spelling?
			// StringTokenizer stringTokenizer = new StringTokenizer(cleanedContent);
			// while (stringTokenizer.hasMoreTokens()) {
			// String token = stringTokenizer.nextToken();
			// String correctedToken = SpellingChecker.getSpellingChecker().checkWord(token);
			// if (correctedToken != null) {
			// cleanedContent = cleanedContent.replaceAll(token, correctedToken);
			// }
			// }
			// logger.info("Cleaned : " + cleanedContent);
			indexable.setContent(cleanedContent);
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

}