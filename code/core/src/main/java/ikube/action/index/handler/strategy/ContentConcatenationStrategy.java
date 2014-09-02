package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Url;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * This strategy will just concatenate all the content in the various indexables
 * and resources into the content field in the 'parent' context object.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02-08-2014
 */
public class ContentConcatenationStrategy extends AStrategy {

    public ContentConcatenationStrategy() {
        this(null);
    }

    public ContentConcatenationStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        concatenateContent(indexable, resource, new StringBuilder());
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        String content = concatenateContent(indexable, resource, new StringBuilder());
        indexable.setContent(content);
        return super.postProcess(indexContext, indexable, document, resource);
    }

    String concatenateContent(final Indexable indexable, final Object resource, final StringBuilder content) {
        if (content.length() > 0) {
            content.append(" ");
        }
        if (Url.class.isAssignableFrom(resource.getClass())) {
            Url url = (Url) resource;
            if (url.getParsedContent() != null) {
                content.append(url.getParsedContent());
            } else {
                content.append(new String(url.getRawContent()));
            }
        } else {
            if (indexable.getContent() != null) {
                content.append(StringUtils.stripToEmpty(indexable.getContent().toString()));
            }
            if (indexable.getChildren() != null) {
                for (final Indexable child : indexable.getChildren()) {
                    concatenateContent(child, resource, content);
                }
            }
        }
        return content.toString().trim();
    }

}