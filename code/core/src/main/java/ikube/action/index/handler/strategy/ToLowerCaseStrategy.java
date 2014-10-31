package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.lucene.document.Document;

/**
 * This strategy will convert all the text in the input into lower case.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-07-2014
 */
public final class ToLowerCaseStrategy extends AStrategy {

    public ToLowerCaseStrategy() {
        this(null);
    }

    public ToLowerCaseStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource)
        throws Exception {
        if (indexable.getContent() != null && String.class.isAssignableFrom(indexable.getContent().getClass())) {
            String content = indexable.getContent().toString();
            content = content.toLowerCase();
            indexable.setContent(content);
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

}