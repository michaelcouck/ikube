package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicLong;

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
     * This is a language that is defined on the strategy, which will be taken into account when applying the logic. It
     * assumes that the language detection strategy is applied before this strategy and that the results are available in the
     * document. If the language is the same as the detected language then the logic sill be applied, otherwise not. It is
     * an optional property, and if no language is specified then all the resources will be processed by the strategy.
     */
    private String language;
    /**
     * Just a counter to print some logging from time to time.
     */
    private AtomicLong atomicLong = new AtomicLong();
    /**
     * The context that contains among other things the actual {@link ikube.analytics.IAnalyzer} object, which is in fact
     * the wrapper interface for the underlying analyzer algorithm. In the default implementation Weka was used as the machine
     * learning library.
     */
    private Context<IAnalyzer<Analysis<Object, Object>, Analysis<Object, Object>, Analysis<Object, Object>>, ?, ?, ?> context;

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
    public boolean aroundProcess(
            final IndexContext indexContext,
            final Indexable indexable,
            final Document document,
            final Object resource)
            throws Exception {
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        // TODO Perhaps detect the subject and the object. Separate the constructs of the sentence for further processing
        // If this data is already classified by another strategy then train the language
        // classifiers on the data. We can then also classify the data and correlate the results
        if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
            boolean process = Boolean.TRUE;
            if (!StringUtils.isEmpty(this.language)) {
                // Configuration specified a language
                String language = document.get(IConstants.LANGUAGE);
                if (logger.isDebugEnabled()) {
                    logger.debug("Language : " + language + " - " + this.language);
                }
                if (StringUtils.isEmpty(language) || !this.language.equals(language)) {
                    // Couldn't find the language or not the correct one so don't train
                    process = Boolean.FALSE;
                }
            }
            if (process) {
                Analysis<Object, Object> analysis = new Analysis<>();
                analysis.setInput(content);
                analysis.setAnalyzer(context.getName());
                //noinspection unchecked
                analysis = analyticsService.analyze(analysis);
                String currentClassification = analysis.getClazz();
                Object output = null;
                if (currentClassification == null) {
                    // This is a regression algorithm, so the result is the first element in
                    // the array of the distribution for the instance, so it would be the price
                    // of the house for example
                    output = analysis.getOutput();
                    if (output != null && output.getClass().isArray()) {
                        Object[] array = (Object[]) output;
                        if (array.length > 0) {
                            // TODO: Must test this again, integration test, with regression specially
                            currentClassification = array[0].toString();
                        }
                    }
                }
                // String currentClassification = context.getAnalyzer().analyze(analysis).getClazz();
                if (currentClassification != null && !StringUtils.isEmpty(currentClassification)) {
                    String previousClassification = document.get(IConstants.CLASSIFICATION);
                    if (previousClassification == null) {
                        addStringField(CLASSIFICATION, currentClassification, indexable, document);
                    } else {
                        if (!currentClassification.equals(previousClassification)) {
                            addStringField(CLASSIFICATION_CONFLICT, currentClassification, indexable, document);
                        }
                    }
                }
                if (atomicLong.getAndIncrement() % 10000 == 0) {
                    logger.warn("Language : " + this.language + ", " + document.get(IConstants.LANGUAGE));
                    logger.warn("Classification : " + currentClassification + ", " + context.getName() + ", " + content);
                }
            }
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setContext(Context<IAnalyzer<Analysis<Object, Object>, Analysis<Object, Object>, Analysis<Object, Object>>, ?, ?, ?> context) {
        this.context = context;
    }
}