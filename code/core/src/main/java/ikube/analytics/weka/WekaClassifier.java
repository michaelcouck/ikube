package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * This is a wrapper for the Weka classifiers. It is essentially a holder with some methods for
 * building and training and using the underlying Weka classification(any one, for example {@link weka
 * .classifiers.functions.SMO}) algorithm.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14-08-2013
 */
public class WekaClassifier extends WekaAnalyzer {

    @Override
    public void init(final Context context) throws Exception {
        super.init(context);
        Instances[] instanceses = (Instances[]) context.getModels();
        for (final Instances instances : instanceses) {
            instances.setClassIndex(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final Context context) throws Exception {
        // TODO: This must be done in parallel
        Object[] filters = context.getFilters();
        String[] evaluations = new String[context.getAlgorithms().length];
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            // Get the components to create the model
            Classifier classifier = (Classifier) context.getAlgorithms()[i];
            Instances instances = (Instances) context.getModels()[i];

            Filter filter = null;
            if (filters != null && filters.length > i) {
                filter = (Filter) filters[i];
            }

            // Filter the data if necessary
            Instances filteredInstances = filter(instances, filter);
            filteredInstances.setRelationName("filtered-instances");

            // And build the model
            logger.info("Building classifier : " + instances.numInstances());
            classifier.buildClassifier(filteredInstances);
            logger.info("Classifier built : " + filteredInstances.numInstances());

            // Set the evaluation of the classifier and the training model
            evaluations[i] = evaluate(classifier, filteredInstances);
        }
        context.setBuilt(Boolean.TRUE);
        context.setEvaluations(evaluations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Context context, final Analysis analysis) throws Exception {
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Instances instances = (Instances) context.getModels()[i];
            Instance instance = instance(analysis.getInput(), instances);
            instance.setClassValue(analysis.getClazz());
            instances.add(instance);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {
        // TODO: Vote the classifiers here!!! In parallel!!!
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Classifier classifier = (Classifier) context.getAlgorithms()[i];
            Instances instances = (Instances) context.getModels()[i];
            Filter filter = (Filter) context.getFilters()[i];

            // Create the instance from the data
            Object input = analysis.getInput();
            Instance instance = instance(input, instances);
            instance.setMissing(0);

            // Classify the instance
            Instance filteredInstance = filter(instance, filter);
            double classification = classifier.classifyInstance(filteredInstance);
            String clazz = instances.classAttribute().value((int) classification);

            // Set the output for the client
            double[] output = classifier.distributionForInstance(filteredInstance);

            analysis.setClazz(clazz);
            analysis.setOutput(output);

            analysis.setAlgorithmOutput(classifier.toString());
            // TODO: Get the correlation co-efficients
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
        Object[] filters = context.getFilters();
        for (int i = 0; i < classifiers.length; i++) {
            Classifier classifier = (Classifier) classifiers[i];

            Filter filter = null;
            if (filters != null && filters.length > i) {
                filter = (Filter) filters[i];
            }

            // Instance filteredInstance = filter(instance, filter);
            Instance filteredInstance = filter((Instance) instance.copy(), filter);
            distributionForInstance[i] = classifier.distributionForInstance(filteredInstance);
        }
        return distributionForInstance;
    }

    private String evaluate(final Classifier classifier, final Instances instances) throws Exception {
        Evaluation evaluation = new Evaluation(instances);
        evaluation.evaluateModel(classifier, instances);
        return evaluation.toSummaryString();
    }

    @SuppressWarnings("UnusedDeclaration")
    private void log(final Instances instances) throws Exception {
        int numClasses = instances.numClasses();
        int numAttributes = instances.numAttributes();
        int numInstances = instances.numInstances();
        String expression = //
            "Classes : " + numClasses + //
                ", instances : " + numInstances +
                ", attributes : " + numAttributes;
        logger.info(expression);
    }

}