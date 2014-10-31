package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;

/**
 * This strategy is typically used during processing in batch, like during an indexing process. It can be added to
 * the strategy chain, and will add the result of the analysis to the document/resource. The analysis tagged document can
 * then be searched, i.e. the analysis results can then become derived analysis results and or aggregated analysis results.
 * <p/>
 * Note that the analysis is distributed in the cluster, so all the servers must have identical analyzers.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02-12-2013
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringJavaAutowiringInspection"})
public class AnalysisStrategy extends AStrategy {

    /**
     * The context that contains among other things the actual {@link ikube.analytics.IAnalyzer} object, which is in fact
     * the wrapper interface for the underlying analyzer algorithm. In the default implementation Weka was used as the machine
     * learning library.
     */
    private Context context;

    /**
     * This is the service that will process the {@link ikube.model.Analysis} object, optionally distributing it into the
     * cluster. In the case the property {@link ikube.model.Analysis#isDistributed()} is true, then a random server will be chosen
     * to execute the analytics on this analysis object.
     */
    @Autowired
    private IAnalyticsService analyticsService;

    public AnalysisStrategy() {
        this(null);
    }

    public AnalysisStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document,
                                 final Object resource) throws Exception {
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
            Analysis<Object, Object> analysis = new Analysis<>();
            analysis.setInput(content);
            analysis.setContext(context.getName());
            analysis = analyticsService.analyze(analysis);
            String classification = analysis.getClazz();
            if (classification != null && !StringUtils.isEmpty(classification)) {
                String previousClassification = document.get(IConstants.CLASSIFICATION);
                if (previousClassification == null) {
                    addStringField(CLASSIFICATION, classification, indexable, document);
                } else if (!classification.equals(previousClassification)) {
                    addStringField(CLASSIFICATION_CONFLICT, classification, indexable, document);
                }
            }
            if (System.currentTimeMillis() % 15000 == 0) {
                logger.warn("Classification : " + classification + ", " + context.getName() + ", " + content);
            }
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    public void setContext(Context context) {
        this.context = context;
    }
}