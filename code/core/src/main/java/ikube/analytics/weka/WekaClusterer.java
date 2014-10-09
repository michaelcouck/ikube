package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import static ikube.analytics.weka.WekaToolkit.filter;

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
    public boolean train(final Context context, final Analysis analysis) throws Exception {
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
    @SuppressWarnings("unchecked")
    public synchronized Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {
        if (!context.isBuilt()) {
            return analysis;
        }

        Filter[] filters = getFilters(context);

        int majorityCluster = 0;
        double[][] distributionForInstance = new double[context.getAlgorithms().length][];

        StringBuilder algorithmsOutput = new StringBuilder();

        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Clusterer clusterer = (Clusterer) context.getAlgorithms()[i];
            Instances instances = (Instances) context.getModels()[i];

            // Create the instance from the data
            Object input = analysis.getInput();
            Instance instance = instance(input, instances);

            // Cluster the instance
            Instance filteredInstance = filter(instance, filters);
            @SuppressWarnings("UnusedDeclaration")
            int cluster = clusterer.clusterInstance(filteredInstance);
            distributionForInstance[i] = clusterer.distributionForInstance(instance);

            if (analysis.isAddAlgorithmOutput()) {
                algorithmsOutput.append(clusterer.toString());
                algorithmsOutput.append("\n\r\n\r");
            }
        }

        analysis.setClazz(Integer.toString(majorityCluster));
        analysis.setOutput(distributionForInstance);
        analysis.setAlgorithmOutput(algorithmsOutput.toString());

        return analysis;
    }

    @Override
    double[][] distributionForInstance(final Context context, final Instance instance) throws Exception {
        double[][] distributionForInstance = new double[context.getAlgorithms().length][];
        Object[] clusterers = context.getAlgorithms();
        Filter[] filters = getFilters(context);
        for (int i = 0; i < clusterers.length; i++) {
            Clusterer clusterer = (Clusterer) clusterers[i];
            Instance filteredInstance = filter((Instance) instance.copy(), filters);
            distributionForInstance[i] = clusterer.distributionForInstance(filteredInstance);
        }
        return distributionForInstance;
    }

}