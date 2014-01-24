package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.toolkit.Timer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public class WekaClusterer extends WekaAnalyzer {

    private Filter filter;
    private volatile Instances instances;
    private volatile Clusterer clusterer;

    private ReentrantLock reentrantLock;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IContext context) throws Exception {
        filter = (Filter) context.getFilter();
        instances = instances(context);
        instances.setRelationName("training_data");
        clusterer = (Clusterer) context.getAlgorithm();
        reentrantLock = new ReentrantLock(Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final IContext context) throws Exception {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    reentrantLock.lock();
                    persist(context, instances);
                    logger.info("Building clusterer : " + instances.numInstances());
                    Instances filteredData = filter(instances, filter);
                    filteredData.setRelationName("filtered_data");
                    clusterer.buildClusterer(filteredData);
                    log();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // As soon as we are finished training with the data, we can
                    // release the memory of all the training instances
                    if (instances.numInstances() >= context.getMaxTraining()) {
                        instances.delete();
                    }
                    reentrantLock.unlock();
                }
            }
        });
        logger.info("Built clusterer in : " + duration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Analysis<String, double[]> analysis) throws Exception {
        try {
            reentrantLock.lock();
            Instance instance = instance(analysis.getInput(), instances);
            instances.add(instance);
            return Boolean.TRUE;
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
            // Create the instance from the data
            Instance instance = instance(analysis.getInput(), instances);
            Instance filteredInstance = filter(instance, filter);
            // Set the output for the client
            int cluster = clusterer.clusterInstance(filteredInstance);
            double[] distributionForInstance = clusterer.distributionForInstance(filteredInstance);
            analysis.setClazz(Integer.toString(cluster));
            analysis.setOutput(distributionForInstance);
            analysis.setAlgorithmOutput(clusterer.toString());
            if (analysis.isCorrelation()) {
                analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
            }
            if (analysis.isDistribution()) {
                Instances filteredData = filter(instances, filter);
                analysis.setDistributionForInstances(getDistributionForInstances(filteredData));
            }
            return analysis;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    double classOrCluster(final Instance instance) throws Exception {
        return clusterer.clusterInstance(instance);
    }

    @Override
    double[] distributionForInstance(final Instance instance) throws Exception {
        return clusterer.distributionForInstance(instance);
    }

    private void log() throws Exception {
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(instances);
        logger.debug("Num clusters : " + clusterEvaluation.clusterResultsToString());
        for (int i = 0; i < instances.numAttributes(); i++) {
            Attribute attribute = instances.attribute(i);
            logger.debug("Attribute : " + attribute.name() + ", " + attribute.type());
            for (int j = 0; j < attribute.numValues(); j++) {
                logger.debug("          : " + attribute.value(j));
            }
        }
    }

}