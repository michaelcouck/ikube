package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class WekaClusterer extends Analyzer {

	private Clusterer clusterer;
	private Instances instances;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final Buildable buildable) throws Exception {
		instances = instances(buildable);
		String type = buildable.getAlgorithmType();
		clusterer = (Clusterer) Class.forName(type).newInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build(final Buildable buildable) throws Exception {
		clusterer.buildClusterer(instances);
		if (buildable.isLog()) {
			log();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean train(final Analysis<String, double[]>... analyses) throws Exception {
		for (final Analysis<String, double[]> analysis : analyses) {
			Instance instance = instance(analysis.getInput(), instances);
			instances.add(instance);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Analysis<String, double[]> analyze(final Analysis<String, double[]> analysis) throws Exception {
		// Create the instance from the data
		Instance instance = instance(analysis.getInput(), instances);

		// Set the output for the client
		int cluster = clusterer.clusterInstance(instance);
		double[] distributionForInstance = clusterer.distributionForInstance(instance);
		analysis.setClazz(cluster);
		analysis.setOutput(distributionForInstance);
		analysis.setAlgorithmOutput(clusterer.toString());
		if (analysis.isCorrelation()) {
			analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
		}
		if (analysis.isDistribution()) {
			analysis.setDistributionForInstances(getDistributionForInstances(instances));
		}

		return analysis;
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
		logger.info("Num clusters : " + clusterEvaluation.clusterResultsToString());
		for (int i = 0; i < instances.numAttributes(); i++) {
			Attribute attribute = instances.attribute(i);
			logger.info("Attribute : " + attribute.name() + ", " + attribute.type());
			for (int j = 0; j < attribute.numValues(); j++) {
				logger.info("          : " + attribute.value(j));
			}
		}
	}

}