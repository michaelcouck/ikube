package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;
import org.apache.commons.lang.StringUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is a classifier for sentiment essentially, i.e. positive/negative. This classifier is based on the {@link SMO} classification algorithm from
 * mWeka, which is a support vector classifier.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14.08.13
 */
public class WekaClassifier extends Analyzer {

    private Filter filter;
    private ReentrantLock reentrantLock;
    private volatile Instances instances;
    private volatile Classifier classifier;

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    public void init(final Buildable buildable) throws Exception {
        instances = instances(buildable);
        instances.setClassIndex(0);
        instances.setRelationName("training_data");
        if (!StringUtils.isEmpty(buildable.getFilterType())) {
            filter = (Filter) Class.forName(buildable.getFilterType()).newInstance();
        }
        String type = buildable.getAlgorithmType();
        classifier = (Classifier) Class.forName(type).newInstance();
        reentrantLock = new ReentrantLock(Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    public void build(final Buildable buildable) {
        try {
            reentrantLock.lock();
            log(instances);

            Instances filteredData = filter(instances, filter);
            filteredData.setRelationName("filtered_data");
            classifier.buildClassifier(filteredData);
            // instances = instances.stringFreeStructure();
            log(filteredData);
            evaluate(filteredData);

            // instances.setRelationName("training_data");
        } catch (Exception e) {
            logger.error("Exception building classifier : ", e);
            instances.delete();
            throw new RuntimeException(e);
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean train(final Analysis<String, double[]>... analyses) throws Exception {
        try {
            reentrantLock.lock();
            for (final Analysis<String, double[]> analysis : analyses) {
                Instance instance = instance(analysis.getInput(), instances);
                instance.setClassValue(analysis.getClazz());
                instances.add(instance);
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Exception creating a training instance : ", e);
            throw new RuntimeException(e);
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analysis<String, double[]> analyze(final Analysis<String, double[]> analysis) throws Exception {
        try {
            reentrantLock.lock();
            if (!StringUtils.isEmpty(analysis.getInput())) {
                // Create the instance from the data
                String input = analysis.getInput();
                Instance instance = instance(input, instances);
                Instance filteredInstance = filter(instance, filter);
                // Classify the instance
                double classification = classifier.classifyInstance(filteredInstance);
                // Set the output for the client
                String clazz = instances.classAttribute().value((int) classification);
                double[] output = classifier.distributionForInstance(filteredInstance);

                analysis.setClazz(clazz);
                analysis.setOutput(output);
                analysis.setAlgorithmOutput(classifier.toString());

                if (logger.isDebugEnabled()) {
                    log(clazz, input, output);
                }

                // analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
                if (analysis.isDistribution()) {
                    analysis.setDistributionForInstances(getDistributionForInstances(instances));
                }
            }
            return analysis;
        } catch (Exception e) {
            String content = analysis.getInput();
            if (!StringUtils.isEmpty(content) && content.length() > 128) {
                content = content.substring(0, 128);
            }
            logger.error("Exception classifying content : " + content, e);
            throw new RuntimeException(e);
        } finally {
            reentrantLock.unlock();
        }
    }

    private void log(final String clazz, final String input, final double[] output) {
        StringBuilder stringBuilder = new StringBuilder();
        for (final double out : output) {
            stringBuilder.append(out);
        }
        logger.info("Class : " + clazz + ", " + input + ", " + stringBuilder);
    }

    @Override
    double classOrCluster(final Instance instance) throws Exception {
        return classifier.classifyInstance(instance);
    }

    @Override
    double[] distributionForInstance(final Instance instance) throws Exception {
        return classifier.distributionForInstance(instance);
    }

    private void evaluate(final Instances instances) throws Exception {
        Evaluation evaluation = new Evaluation(instances);
        evaluation.evaluateModel(classifier, instances);
        String evaluationReport = evaluation.toSummaryString();
        logger.info("Classifier evaluation : " + evaluationReport);
    }

    private void log(final Instances instances) throws Exception {
        if (instances != null && instances.numInstances() > 0 && instances.numClasses() > 0 && instances.numAttributes() > 0) {
            int numClasses = instances.numClasses();
            int numAttributes = instances.numAttributes();
            int numInstances = instances.numInstances();
            Object[] parameters = {numClasses, numAttributes, numInstances, classifier.hashCode()};
            logger.info("Building classifier, classes : {}, attributes : {}, instances : {}, classifier : {} ", parameters);
        }
    }

}