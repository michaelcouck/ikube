package ikube.analytics.weka;

import com.google.common.collect.Lists;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
        List<Future> futures = Lists.newArrayList();
        final String[] evaluations = new String[context.getAlgorithms().length];
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            final int index = i;
            Future<?> future = ThreadUtilities.submit(this.getClass().getName(), new Runnable() {
                public void run() {
                    try {
                        // Get the components to create the model
                        Clusterer clusterer = (Clusterer) context.getAlgorithms()[index];
                        Instances instances = (Instances) context.getModels()[index];
                        Filter filter = getFilter(context, index);

                        // Filter the data if necessary
                        Instances filteredInstances = WekaClusterer.this.filter(instances, filter);
                        filteredInstances.setRelationName("filtered-instances");

                        // And build the model
                        logger.info("Building clusterer : " + instances.numInstances());
                        clusterer.buildClusterer(filteredInstances);
                        logger.info("Clusterer built : " + filteredInstances.numInstances());

                        // Set the evaluation
                        evaluations[index] = evaluate(clusterer, instances);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            futures.add(future);
        }
        ThreadUtilities.waitForAnonymousFutures(futures, Long.MAX_VALUE);
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
            instances.add(instance);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {
        int majorityCluster = 0;
        double[] majorityDistributionForInstance = null;
        Map<Integer, AtomicInteger> clusters = new HashMap<>();
        StringBuilder algorithmsOutput = new StringBuilder();

        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Clusterer clusterer = (Clusterer) context.getAlgorithms()[i];
            Instances instances = (Instances) context.getModels()[i];

            Filter filter = getFilter(context, i);

            // Create the instance from the data
            Object input = analysis.getInput();
            Instance instance = instance(input, instances);

            // Cluster the instance
            Instance filteredInstance = filter(instance, filter);
            int cluster = clusterer.clusterInstance(filteredInstance);
            double[] distributionForInstance = clusterer.distributionForInstance(instance);

            AtomicInteger count = clusters.get(cluster);
            if (count == null) {
                count = new AtomicInteger(0);
                clusters.put(cluster, count);
            }
            count.incrementAndGet();
            if (majorityDistributionForInstance == null) {
                majorityCluster = cluster;
                majorityDistributionForInstance = distributionForInstance;
            }
            if (count.get() > clusters.get(majorityCluster).get()) {
                majorityCluster = cluster;
                majorityDistributionForInstance = distributionForInstance;
            }

            algorithmsOutput.append(clusterer.toString());
            algorithmsOutput.append("\n\r\n\r");
            // TODO: Get the correlation co-efficients
        }
        analysis.setClazz(Integer.toString(majorityCluster));
        analysis.setOutput(majorityDistributionForInstance);
        analysis.setAlgorithmOutput(algorithmsOutput.toString());

        return analysis;
    }

    @Override
    double[][] distributionForInstance(final Context context, final Instance instance) throws Exception {
        double[][] distributionForInstance = new double[context.getAlgorithms().length][];
        Object[] clusterers = context.getAlgorithms();
        for (int i = 0; i < clusterers.length; i++) {
            Clusterer clusterer = (Clusterer) clusterers[i];
            Filter filter = getFilter(context, i);
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