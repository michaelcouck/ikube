package ikube.analytics;

import ikube.model.Buildable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClusterer.class);

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
	public boolean train(final String... strings) throws Exception {
		for (final String string : strings) {
			Instance instance = instance(string, instances);
			instances.add(instance);
		}
		return true;
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
	public String analyze(final String input) throws Exception {
		Instance instance = instance(input, instances);
		int cluster = clusterer.clusterInstance(instance);
		return Integer.toString(cluster);
	}

	private void log() throws Exception {
		ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
		clusterEvaluation.setClusterer(clusterer);
		clusterEvaluation.evaluateClusterer(instances);
		LOGGER.info("Num clusters : " + clusterEvaluation.clusterResultsToString());
		for (int i = 0; i < instances.numAttributes(); i++) {
			Attribute attribute = instances.attribute(i);
			LOGGER.info("Attribute : " + attribute.name() + ", " + attribute.type());
			for (int j = 0; j < attribute.numValues(); j++) {
				LOGGER.info("          : " + attribute.value(j));
			}
		}
	}

}