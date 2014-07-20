package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.lucene.document.Document;

/**
 * This strategy as the name suggests, will train a classifier. The assumption is that a previous strategy will
 * have somehow already classified the resource, perhaps with the emoticons as the Twitter emoticon strategy does,
 * and will then feed the resource to the classifier to train, including the class/category.
 * <p/>
 * Note that the analysis training is distributed in the cluster, so all the servers must have identical analyzers.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02-12-2013
 */
@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class ClassifierTrainingStrategy extends AStrategy {

    public ClassifierTrainingStrategy() {
        this(null);
    }

    public ClassifierTrainingStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document,
                                 final Object resource) throws Exception {
        // There are no moe tweets, so training it dead now
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    public void initialize() {
    }

}