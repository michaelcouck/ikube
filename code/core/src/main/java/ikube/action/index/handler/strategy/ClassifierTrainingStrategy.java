package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

import java.util.HashMap;
import java.util.Map;

import static ikube.IConstants.CLASSIFICATION;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 02.12.13
 */
public class ClassifierTrainingStrategy extends AStrategy {

    private String language;
    private Context<?, ?, ?, ?> context;
    private Map<String, Boolean> trained;

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
    public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable,
                                 final Document document, final Object resource) throws Exception {
        String language = document.get(IConstants.LANGUAGE);
        String classification = document.get(CLASSIFICATION);
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        if (language != null && this.language.equals(language) &&
            !StringUtils.isEmpty(classification) && !StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
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
        IAnalyzer classifier = (IAnalyzer) context.getAnalyzer();
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setClazz(clazz);
        analysis.setInput(content);
        int classSize = classifier.sizeForClassOrCluster(analysis);
        trained = classSize == 0 || classSize >= context.getMaxTraining();
        this.trained.put(clazz, trained);
        try {
            classifier.train(analysis);
            if (classSize % 10 == 0) {
                logger.info("Training : " + clazz + ", language : " + this.language + ", class size : " + classSize);
            }
            if (classSize % 100 == 0) {
                classifier.build(context);
            }
        } catch (final Exception e) {
            logger.error("Exception building classifier : ", e);
        }
    }

    public void initialize() {
        trained = new HashMap<>();
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setContext(Context<?, ?, ?, ?> context) {
        this.context = context;
    }
}