package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * This class will replace aaaaaahhhhhh type tokens with aahh, removing the characters that are repeated too many times.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-07-2013
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
    public boolean aroundProcess(
            final IndexContext<?> indexContext,
            final Indexable indexable,
            final Document document,
            final Object resource)
            throws Exception {
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        String cleanedContent = cleanContent(content);
        indexable.setContent(cleanedContent);
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    String cleanContent(final String content) {
        if (!StringUtils.isEmpty(content)) {
            StringBuilder stringBuilder = new StringBuilder();
            // Remove multiple characters, like aaaaahhhhhyyyeeeah, to aahhyyeeah
            char[] chars = content.toCharArray();
            for (int i = 0, j = 1; i < chars.length; i++, j++) {
                char c = chars[i];
                if (i != 0 && c == chars[i - 1] && chars.length > j && c == chars[j]) {
                    continue;
                }
                stringBuilder.append(c);
            }
            return stringBuilder.toString().toLowerCase();
        }
        return content;
    }

}