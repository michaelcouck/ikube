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
        concatenateContent(indexable, resource);
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        concatenateContent(indexable, resource);
        return super.postProcess(indexContext, indexable, document, resource);
    }

    void concatenateContent(final Indexable indexable, final Object resource) {
        StringBuilder content = new StringBuilder();
        if (Url.class.isAssignableFrom(resource.getClass())) {
            content.append(" \n\r");
            Url url = (Url) resource;
            if (url.getParsedContent() != null) {
                content.append(url.getParsedContent());
            } else {
                content.append(new String(url.getRawContent()));
            }
        }
        if (indexable.getContent() != null) {
            content.append(StringUtils.stripToEmpty(indexable.getContent().toString()));
        }
        indexable.setContent(content.toString());
    }

}