package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * This class is the Weka clusterer wrapper. Ultimately just wrapping the Weka
 * algorithm(any one of the clustering algorithms) and adds some easier to understand
 * methods for building and training said algorithms.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public class WekaClusterer extends WekaAnalyzer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final Context context) throws Exception {
        // TODO: This must be done in parallel
        Filter[] filters = (Filter[]) context.getFilters();
        String[] evaluations = new String[context.getAlgorithms().length];
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            // Get the components to create the model
            Clusterer clusterer = (Clusterer) context.getAlgorithms()[i];
            Instances instances = (Instances) context.getModels()[i];

            Filter filter = null;
            if (filters != null && filters.length > i) {
                filter = filters[i];
            }

            // Filter the data if necessary
            Instances filteredInstances = filter(instances, filter);
            filteredInstances.setRelationName("filtered-instances");

            // And build the model
            logger.info("Building clusterer : " + instances.numInstances());
            clusterer.buildClusterer(filteredInstances);
            logger.info("Clusterer built : " + filteredInstances.numInstances());

            // Set the evaluation
            evaluations[i] = evaluate(clusterer, instances);
        }
        context.setEvaluations(evaluations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Context context, final Analysis<Object, Object> analysis) throws Exception {
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Instances instances = (Instances) context.getModels()[i];
            Instance instance = instance(analysis.getInput(), instances);
            instances.add(instance);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analysis<Object, Object> analyze(final Context context, final Analysis<Object, Object> analysis) throws Exception {
        // TODO: Vote the clusterers here!!! In parallel!!!
        Filter[] filters = (Filter[]) context.getFilters();
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Clusterer clusterer = (Clusterer) context.getAlgorithms()[i];
            Instances instances = (Instances) context.getModels()[i];

            Filter filter = null;
            if (filters != null && filters.length > i) {
                filter = filters[i];
            }

            // Create the instance from the data
            Object input = analysis.getInput();
            Instance instance = instance(input, instances);

            // Cluster the instance
            Instance filteredInstance = filter(instance, filter);
            int cluster = clusterer.clusterInstance(filteredInstance);

            // Set the output for the client
            double[][] output = distributionForInstance(context, instance);

            analysis.setClazz(Integer.toString(cluster));
            analysis.setOutput(output);

            analysis.setAlgorithmOutput(clusterer.toString());
            // TODO: Get the correlation co-efficients
        }

        return analysis;
    }

    @Override
    double[][] distributionForInstance(final Context context, final Instance instance) throws Exception {
        double[][] distributionForInstance = new double[context.getAlgorithms().length][];
        Object[] clusterers = context.getAlgorithms();
        Object[] filters = context.getFilters();
        for (int i = 0; i < clusterers.length; i++) {
            Clusterer clusterer = (Clusterer) clusterers[i];

            Filter filter = null;
            if (filters != null && filters.length > i) {
                filter = (Filter) filters[i];
            }

            // Instance filteredInstance = filter(instance, filter);
            Instance filteredInstance = filter((Instance) instance.copy(), filter);
            distributionForInstance[i] = clusterer.distributionForInstance(filteredInstance);
        }
        return distributionForInstance;
    }

    private String evaluate(final Clusterer clusterer, final Instances instances) throws Exception {
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(instances);
        return clusterEvaluation.clusterResultsToString();
    }

}