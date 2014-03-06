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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static ikube.IConstants.CLASSIFICATION;

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
public class ClassifierTrainingStrategy extends AStrategy {

    private String language;
    private int buildThreshold = 100;
    private Context<?, ?, ?, ?> context;
    private Map<String, Boolean> trained;
    private ReentrantLock reentrantLock;

    @Autowired
    private IAnalyticsService analyticsService;

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
    public boolean aroundProcess(
            final IndexContext<?> indexContext,
            final Indexable<?> indexable,
            final Document document,
            final Object resource)
            throws Exception {
        String language = document.get(IConstants.LANGUAGE);
        String classification = document.get(CLASSIFICATION);
        String content = indexable.getContent() != null ?
                indexable.getContent().toString() : resource != null ?
                resource.toString() : null;
        if (language != null &&
                this.language.equals(language) &&
                !StringUtils.isEmpty(classification) &&
                !StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
            train(classification, content);
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    @SuppressWarnings("unchecked")
    void train(final String clazz, final String content) throws Exception {
        Boolean trained = this.trained.get(clazz);
        if (trained != null && trained) {
            return;
        }
        try {
            reentrantLock.lock();
            // IAnalyzer classifier = (IAnalyzer) context.getAnalyzer();
            Analysis<String, double[]> analysis = new Analysis<>();
            analysis.setClazz(clazz);
            analysis.setInput(content);
            analysis.setAnalyzer(context.getName());
            analysis = analyticsService.sizesForClassesOrClusters(analysis);

            int indexOfClass = 0;
            Object[] classesOrClusters = analysis.getClassesOrClusters();
            for (int i = 0; i < classesOrClusters.length; i++) {
                if (clazz.equals(classesOrClusters[i])) {
                    indexOfClass = i;
                    break;
                }
            }
            int classSize = analysis.getSizesForClassesOrClusters()[indexOfClass];

            // int classSize = classifier.sizeForClassOrCluster(analysis);
            trained = classSize == 0 || classSize >= context.getMaxTraining();
            this.trained.put(clazz, trained);
            try {
                analyticsService.train(analysis);
                // classifier.train(analysis);
                if (classSize % 10 == 0) {
                    Object[] parameters = {clazz, this.language, classSize};
                    logger.info("Training : , language : , class size : ", parameters);
                }
                if (buildThreshold == 0) {
                    buildThreshold = 100;
                }
                if (classSize % buildThreshold == 0) {
                    Object[] parameters = {clazz, this.language, classSize, buildThreshold};
                    logger.info("Building : {}, language : {}, class size : {}, , build threshold : {}", parameters);
                    analyticsService.build(analysis);
                    // classifier.build(context);
                    buildThreshold = classSize / 10;
                }
            } catch (final Exception e) {
                logger.error("Exception building classifier : ", e);
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    public void initialize() {
        trained = new HashMap<>();
        reentrantLock = new ReentrantLock(Boolean.TRUE);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setBuildThreshold(int buildThreshold) {
        this.buildThreshold = buildThreshold;
    }

    public void setContext(Context<?, ?, ?, ?> context) {
        this.context = context;
    }
}