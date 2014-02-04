package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is a classifier for sentiment essentially, i.e. positive/negative. This classifier is based on the {@link SMO} classification algorithm from
 * mWeka, which is a support vector classifier. This class is thread safe.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14.08.13
 */
public class WekaClassifier extends WekaAnalyzer {

    private Filter filter;
    private Instances instances;
    private ReentrantLock analyzeLock;

    private volatile Classifier classifier;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Context context) throws Exception {
        filter = (Filter) context.getFilter();
        instances = instances(context);
        instances.setClassIndex(0);
        instances.setRelationName("training_data");
        classifier = (Classifier) context.getAlgorithm();
        analyzeLock = new ReentrantLock(Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final Context context) {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            @SuppressWarnings("unchecked")
            public void execute() {
                try {
                    analyzeLock.lock();
                    log(instances);
                    persist(context, instances);
                    logger.info("Building classifier : " + instances.numInstances());

                    Instances filteredData = filter(instances, filter);
                    filteredData.setRelationName("filtered_data");
                    classifier.buildClassifier(filteredData);

                    log(filteredData);
                    evaluate(filteredData);
                } catch (final Exception e) {
                    instances.delete();
                    throw new RuntimeException(e);
                } finally {
                    // As soon as we are finished training with the data, we can
                    // release the memory of all the training instances
                    if (instances.numInstances() >= context.getMaxTraining()) {
                        instances.delete();
                    }
                    analyzeLock.unlock();
                }
            }
        });
        logger.info("Built classifier in : " + duration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Analysis<String, double[]> analysis) throws Exception {
        try {
            analyzeLock.lock();
            Instance instance = instance(analysis.getInput(), instances);
            instance.setClassValue(analysis.getClazz());
            instances.add(instance);
            return Boolean.TRUE;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            analyzeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analysis<String, double[]> analyze(final Analysis<String, double[]> analysis) throws Exception {
        try {
            analyzeLock.lock();
            if (!StringUtils.isEmpty(analysis.getInput())) {
                // Create the instance from the data
                String input = analysis.getInput();
                Instance instance = instance(input, instances);
                // Classify the instance
                double classification = classOrCluster(instance);
                // Set the output for the client
                String clazz = instances.classAttribute().value((int) classification);
                double[] output = distributionForInstance(instance);

                analysis.setClazz(clazz);
                analysis.setOutput(output);
                analysis.setAlgorithmOutput(classifier.toString());
                log(clazz, input, output);

                if (analysis.isCorrelation()) {
                    analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
                }
                if (analysis.isDistribution()) {
                    analysis.setDistributionForInstances(getDistributionForInstances(instances));
                }
            }
            return analysis;
        } catch (final Exception e) {
            String content = analysis.getInput();
            if (!StringUtils.isEmpty(content) && content.length() > 128) {
                content = content.substring(0, 128);
            }
            logger.error("Exception classifying content : " + content, e);
            throw new RuntimeException(e);
        } finally {
            analyzeLock.unlock();
        }
    }

    @Override
    public void destroy(final Context context) throws Exception {
        instances.delete();
    }

    @Override
    double classOrCluster(final Instance instance) throws Exception {
        Instance filteredInstance = filter(instance, filter);
        return classifier.classifyInstance(filteredInstance);
    }

    @Override
    double[] distributionForInstance(final Instance instance) throws Exception {
        Instance filteredInstance = filter(instance, filter);
        return classifier.distributionForInstance(filteredInstance);
    }

    private void evaluate(final Instances instances) throws Exception {
        Evaluation evaluation = new Evaluation(instances);
        evaluation.evaluateModel(classifier, instances);
        String evaluationReport = evaluation.toSummaryString();
        logger.debug("Classifier evaluation : " + evaluationReport);
    }

    private void log(final String clazz, final String input, final double[] output) {
        StringBuilder stringBuilder = new StringBuilder();
        for (final double out : output) {
            stringBuilder.append(out);
        }
        logger.debug("Class : " + clazz + ", " + input + ", " + stringBuilder);
    }

    private void log(final Instances instances) throws Exception {
        if (!logger.isDebugEnabled()) {
            return;
        }
        if (instances != null && instances.numInstances() > 0 && instances.numClasses() > 0 && instances.numAttributes() > 0) {
            int numClasses = instances.numClasses();
            int numAttributes = instances.numAttributes();
            int numInstances = instances.numInstances();
            String expression = //
                "Building classifier, classes : " + //
                    numClasses + //
                    ", attributes : " + //
                    numAttributes + //
                    ", instances : " + //
                    numInstances + //
                    ", classifier :  " + //
                    classifier.hashCode();
            logger.debug(expression);
        }
    }

}