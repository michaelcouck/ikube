package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * This class is a classifier for sentiment essentially, i.e. positive/negative. This classifier is based on the {@link SMO} classification algorithm from
 * mWeka, which is a support vector classifier.
 * 
 * @author Michael Couck
 * @since 14.08.13
 * @version 01.00
 */
public class WekaClassifier extends Analyzer {

	private Filter filter;
	private volatile Instances instances;
	private volatile Classifier classifier;
	private ReentrantLock reentrantLock;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	public void init(final Buildable buildable) throws Exception {
		instances = instances(buildable);
		instances.setClassIndex(0);
		if (!StringUtils.isEmpty(buildable.getFilterType())) {
			filter = (Filter) Class.forName(buildable.getFilterType()).newInstance();
		}
		String type = buildable.getAlgorithmType();
		classifier = (Classifier) Class.forName(type).newInstance();
		reentrantLock = new ReentrantLock(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(final Buildable buildable) {
		try {
			reentrantLock.lock();

			Instances filteredData = filter(instances, filter);

			classifier.buildClassifier(filteredData);
			instances = instances.stringFreeStructure();
			log(filteredData);

			instances.setRelationName("training_data");
			filteredData.setRelationName("filtered_data");
		} catch (Exception e) {
			logger.info("Exception building classifier : ", e);
			instances.delete();
			throw new RuntimeException(e);
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
				instance.setClassValue(analysis.getClazz().toString());
				instances.add(instance);
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			logger.error("Exception creating a training instance : ", e);
			throw new RuntimeException(e);
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
			if (!StringUtils.isEmpty(analysis.getInput())) {
				// Create the instance from the data
				Instance instance = instance(analysis.getInput(), instances);
				Instance filteredInstance = filter(instance, filter);
				// Classify the instance
				double classification = classifier.classifyInstance(filteredInstance);
				// Set the output for the client
				String clazz = instances.classAttribute().value((int) classification);
				double[] output = classifier.distributionForInstance(filteredInstance);
				analysis.setClazz(clazz);
				analysis.setOutput(output);
				analysis.setAlgorithmOutput(classifier.toString());
				// analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
				if (analysis.isDistribution()) {
					analysis.setDistributionForInstances(getDistributionForInstances(instances));
				}
			}
			return analysis;
		} catch (Exception e) {
			String content = analysis.getInput();
			if (!StringUtils.isEmpty(content) && content.length() > 128) {
				content = content.substring(0, 128);
			}
			logger.error("Exception classifying content : " + analysis.getInput(), e);
			throw new RuntimeException(e);
		} finally {
			// Clear the instances every so often to avoid an out of memory
			if (instances.numInstances() > 1000) {
				instances.delete();
			}
			reentrantLock.unlock();
		}
	}

	@Override
	double classOrCluster(final Instance instance) throws Exception {
		return classifier.classifyInstance(instance);
	}

	@Override
	double[] distributionForInstance(final Instance instance) throws Exception {
		return classifier.distributionForInstance(instance);
	}

	private void log(final Instances instances) throws Exception {
		try {
			reentrantLock.lock();
			if (instances != null) {
				int numClasses = instances.numClasses();
				int numAttributes = instances.numAttributes();
				int numInstances = instances.numInstances();
				logger.info("Building classifier, classes : " + numClasses + ", attributes : " + numAttributes + ", instances : " + numInstances
						+ ", classifier : " + classifier.hashCode());
				Evaluation evaluation = new Evaluation(instances);
				evaluation.evaluateModel(classifier, instances);
				String evaluationReport = evaluation.toSummaryString();
				logger.debug("Classifier evaluation : " + evaluationReport);
			}
		} finally {
			reentrantLock.unlock();
		}
	}

}