package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.Arrays;

import static ikube.analytics.weka.WekaToolkit.filter;

/**
 * This is a wrapper for the Weka classifiers. It is essentially a holder with some
 * methods for building and training and using the underlying Weka classification(any one, for
 * example {@link weka.classifiers.functions.SMO}) algorithm.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14-08-2013
 */
public class WekaClassifier extends WekaAnalyzer {

    @Override
    public void init(final Context context) {
        try {
            super.init(context);
            Instances[] instancesArray = (Instances[]) context.getModels();
            if (instancesArray != null) {
                for (final Instances instances : instancesArray) {
                    if (instances != null) {
                        instances.setClassIndex(0);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Exception building analyzer : ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Context context, final Analysis analysis) throws Exception {
        Object[] models = context.getModels();
        for (final Object model : models) {
            Instances instances = (Instances) model;
            Instance instance = instance(analysis.getInput(), instances);
            if (analysis.getClazz() != null) {
                instance.setClassValue(analysis.getClazz());
            }
            instances.add(instance);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {

        try {
            if (!context.isBuilt()) {
                return analysis;
            }
            String majorityClass = null;
            double[] majorityDistributionForInstance = null;
            StringBuilder algorithmsOutput = new StringBuilder();
            Filter[] filters = getFilters(context);

            double highestProbability = -1.0;
            for (int i = 0; i < context.getAlgorithms().length; i++) {
                Classifier classifier = (Classifier) context.getAlgorithms()[i];
                Instances instances = (Instances) context.getModels()[i];

                // Create the instance from the data
                Object input = analysis.getInput();
                Instance instance = instance(input, instances);
                instance.setMissing(0);

                // Classify the instance
                Instance filteredInstance = filter(instance, filters);
                double classification = classifier.classifyInstance(filteredInstance);
                String clazz = instances.classAttribute().value((int) classification);
                double[] distributionForInstance = classifier.distributionForInstance(filteredInstance);
                if (logger.isDebugEnabled()) {
                    logger.error("Class : " + clazz + ", dist : " + Arrays.toString(distributionForInstance));
                }

                // Note: The distribution for instances for the instance coming from the
                // classifier must be the probability for each of the classes. In the SMO
                // class this is seemingly not possible, as such this will probably still
                // perform properly, but there may be edge cases where the probabilities do
                // not correspond to the aggregate of the classifiers
                for (final double probability : distributionForInstance) {
                    if (probability > highestProbability) {
                        majorityClass = clazz;
                        highestProbability = probability;
                        majorityDistributionForInstance = distributionForInstance;
                    }
                }

                if (analysis.isAddAlgorithmOutput()) {
                    algorithmsOutput.append(classifier.toString());
                    algorithmsOutput.append("\n\r\n\r");
                }
            }

            analysis.setClazz(majorityClass);
            analysis.setOutput(majorityDistributionForInstance);
            analysis.setAlgorithmOutput(algorithmsOutput.toString());
        } catch (final Exception e) {
            logger.error("", e);
        }

        return analysis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    double[][] distributionForInstance(final Context context, final Instance instance) throws Exception {
        double[][] distributionForInstance = new double[context.getAlgorithms().length][];
        Object[] classifiers = context.getAlgorithms();
        Filter[] filters = getFilters(context);
        for (int i = 0; i < classifiers.length; i++) {
            Classifier classifier = (Classifier) classifiers[i];
            // Instance filteredInstance = filter(instance, filter);
            Instance filteredInstance = filter((Instance) instance.copy(), filters);
            distributionForInstance[i] = classifier.distributionForInstance(filteredInstance);
        }
        return distributionForInstance;
    }

}