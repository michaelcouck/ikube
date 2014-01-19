package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;

import java.util.concurrent.locks.ReentrantLock;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 02.12.13
 */
public class ClassifierTrainingStrategy extends AStrategy {

    /**
     * The maximum number of positive training folds
     */
    private int positive;
    /**
     * The maximum number of negative training folds
     */
    private int negative;
    /**
     * The maximum number of neutral training folds
     */
    private int neutral;
    /**
     * In the event this should only train on a specific language.
     */
    private String language;
    /**
     * The number of instances in training before rebuilding the classifier.
     */
    private int rebuildingCount = 10;

    /**
     * The wrapper for the 'real' classifier, probably Weka
     */
    private IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> classifier;

    /**
     * The number of instances used for training.
     */
    private int trainingCount = 0;
    /**
     * Lock to ensure that multiple threads don't try to train at the same time.
     */
    private ReentrantLock reentrantLock = new ReentrantLock();

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
    public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
        throws Exception {
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
            boolean train = Boolean.TRUE;
            if (!StringUtils.isEmpty(this.language)) {
                // Configuration specified a language
                String language = document.get(IConstants.LANGUAGE);
                if (StringUtils.isEmpty(language) || !this.language.equals(language)) {
                    // Couldn't find the language or not the correct one so don't train
                    train = Boolean.FALSE;
                }
            }
            if (train) {
                String classification = document.get(CLASSIFICATION);
                if (!StringUtils.isEmpty(classification)) {
                    train(classification, content);
                }
            }
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    @SuppressWarnings("unchecked")
    void train(final String clazz, final String content) {
        if (IConstants.POSITIVE.equals(clazz)) {
            if (positive < 0) {
                return;
            }
            positive--;
        } else if (IConstants.NEGATIVE.equals(clazz)) {
            if (negative < 0) {
                return;
            }
            negative--;
        } else if (IConstants.NEUTRAL.equals(clazz)) {
            if (neutral < 0) {
                return;
            }
            neutral--;
        } else {
            // logger.info("Can't train with class : " + clazz);
            return;
        }
        try {
            reentrantLock.lock();
            Analysis<String, double[]> analysis = new Analysis<>();
            analysis.setClazz(clazz);
            analysis.setInput(content);
            classifier.train(analysis);
            trainingCount++;
            if (trainingCount % rebuildingCount == 0) {
                classifier.build(null);
            }
        } catch (Exception e) {
            logger.error("Exception building classifier : ", e);
        } finally {
            reentrantLock.unlock();
        }
    }

    public void setPositive(final int positive) {
        this.positive = positive;
    }

    public void setNegative(final int negative) {
        this.negative = negative;
    }

    public void setNeutral(final int neutral) {
        this.neutral = neutral;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public void setRebuildingCount(final int rebuildingCount) {
        this.rebuildingCount = rebuildingCount;
    }

    public void setClassifier(final IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> classifier) {
        this.classifier = classifier;
    }

}