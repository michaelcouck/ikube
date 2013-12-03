package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * @author Michael Couck
 * @since 03.12.13
 * @version 01.00
 */
public final class LanguageFilterStrategy extends AStrategy {
	
	private static final char sp = ' ';

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
		if (!StringUtils.isEmpty(content)) {
			StringBuilder b = new StringBuilder();
			// Remove single characters and numbers and anything that isn't human
			// and strips the whitespace to one character if there are more than one
			// or a character is removed
			char[] cs = content.toCharArray();
			char p = sp;
			for (int i = 0; i < cs.length; i++) {
				char c = cs[i];
				boolean a = Boolean.FALSE;
				if (Character.isWhitespace(c)) {
					if (p != sp) {
						a = Boolean.TRUE;
					}
				} else if (Character.isAlphabetic(c)) {
					a = Boolean.TRUE;
				} else {
					if (p != ' ') {
						p = sp;
						b.append(sp);
					}
				}
				if (a) {
					p = c;
					b.append(c);
				}
			}
			return b.toString().toLowerCase();
		}
		return content;
	}

}