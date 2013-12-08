package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class WekaClusterer extends Analyzer {

	private Filter filter;
	private Clusterer clusterer;
	private Instances instances;
	private ReentrantLock reentrantLock;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final Buildable buildable) throws Exception {
		instances = instances(buildable);
		if (!StringUtils.isEmpty(buildable.getFilterType())) {
			filter = (Filter) Class.forName(buildable.getFilterType()).newInstance();
		}
		String type = buildable.getAlgorithmType();
		clusterer = (Clusterer) Class.forName(type).newInstance();
		reentrantLock = new ReentrantLock(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build(final Buildable buildable) throws Exception {
		try {
			reentrantLock.lock();
			Instances filteredData = filter(instances, filter);
			clusterer.buildClusterer(filteredData);
			instances.setRelationName("training_data");
			filteredData.setRelationName("filtered_data");
			if (buildable.isLog()) {
				log();
			}
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
				instances.add(instance);
			}
		} finally {
			reentrantLock.unlock();
		}
		return true;
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
			analysis.setClazz(cluster);
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