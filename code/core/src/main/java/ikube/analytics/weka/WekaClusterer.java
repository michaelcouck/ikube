package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.Timer;
import org.apache.commons.lang.StringUtils;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public class WekaClusterer extends WekaAnalyzer {

    private volatile Clusterer clusterer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Context context) throws Exception {
        super.init(context);
        clusterer = (Clusterer) context.getAlgorithm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final Context context) throws Exception {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            @SuppressWarnings("unchecked")
            public void execute() {
                try {
                    analyzeLock.lock();
                    persist(context, instances);
                    logger.info("Building clusterer : " + instances.numInstances());

                    Instances filteredData = filter(instances, filter);
                    filteredData.setRelationName("filtered_data");
                    clusterer.buildClusterer(filteredData);

                    if (logger.isDebugEnabled()) {
                        log();
                    }
                } catch (final Exception e) {
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
        logger.info("Built clusterer in : " + duration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Analysis<String, double[]> analysis) throws Exception {
        try {
            analyzeLock.lock();
            Instance instance = instance(analysis.getInput(), instances);
            instances.add(instance);
            return Boolean.TRUE;
        } finally {
            analyzeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analysis<String, double[]> analyze(final Analysis<String, double[]> analysis) throws Exception {
        if (analyzeLock.isLocked()) {
            return analysis;
        }
        try {
            analyzeLock.lock();
            // Create the instance from the data
            String input = analysis.getInput();
            Instance instance = instance(input, instances);
            // Set the output for the client
            int cluster = (int) classOrCluster(instance);
            double[] distributionForInstance = distributionForInstance(instance);

            analysis.setClazz(Integer.toString(cluster));
            analysis.setOutput(distributionForInstance);
            // TODO This must be optional as it is expensive
            analysis.setAlgorithmOutput(clusterer.toString());
            if (analysis.isCorrelation()) {
                analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
            }
            if (analysis.isDistribution()) {
                analysis.setDistributionForInstances(getDistributionForInstances(instances));
            }
            return analysis;
        } catch (final Exception e) {
            String content = analysis.getInput();
            if (!StringUtils.isEmpty(content) && content.length() > 128) {
                content = content.substring(0, 128);
            }
            logger.error("Exception clustering content : " + content, e);
            throw new RuntimeException(e);
        } finally {
            analyzeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] classesOrClusters() throws Exception {
        Object[] clusters = new Object[clusterer.numberOfClusters()];
        for (double i = 0F; i < clusters.length; i++) {
            clusters[(int) i] = i;
        }
        return clusters;
    }

    @Override
    double classOrCluster(final Instance instance) throws Exception {
        Instance filteredInstance = filter(instance, filter);
        return clusterer.clusterInstance(filteredInstance);
    }

    @Override
    double[] distributionForInstance(final Instance instance) throws Exception {
        Instance filteredInstance = filter(instance, filter);
        return clusterer.distributionForInstance(filteredInstance);
    }

    private void log() throws Exception {
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(instances);
        logger.info("Num clusters : " + clusterer.numberOfClusters());
        logger.info("Cluster results : " + clusterEvaluation.clusterResultsToString());
        for (int i = 0; i < instances.numAttributes(); i++) {
            Attribute attribute = instances.attribute(i);
            logger.info("Attribute : " + attribute.name() + ", " + attribute.type());
            for (int j = 0; j < attribute.numValues(); j++) {
                logger.debug("          : " + attribute.value(j));
            }
        }
    }

}