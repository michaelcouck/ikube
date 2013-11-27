package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private Filter filter;
	private volatile Instances instances;
	private volatile Classifier classifier;

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
	}

	/**
	 * This method will build the classifier again using the training instances. When the training instances get to a certain number we can re-build the
	 * classifier from the training data. We catch all exceptions and clean the training instance data set of all the instances that are a problem.
	 */
	public synchronized void build(final Buildable buildable) {
		try {
			log(null);

			Instances filteredData = null;

			if (filter == null) {
				filteredData = instances;
			} else {
				filter.setInputFormat(instances);
				filteredData = Filter.useFilter(instances, filter);
			}

			Classifier classifier = new SMO();
			classifier.buildClassifier(filteredData);
			this.classifier = classifier;
			instances = instances.stringFreeStructure();

			filteredData.setRelationName("filtered_data");
			instances.setRelationName("training_data");
			log(filteredData);
			return;
		} catch (Exception e) {
			LOGGER.info("Exception building classifier : ", e);
			instances.delete();
			// classificationInstances.delete();
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean train(final Analysis<String, double[]>... analyses) throws Exception {
		try {
			for (final Analysis<String, double[]> analysis : analyses) {
				Instance instance = instance(analysis.getInput(), instances);
				instance.setClassValue(analysis.getClazz().toString());
				instances.add(instance);
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			LOGGER.error("Exception creating a training instance : ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Analysis<String, double[]> analyze(final Analysis<String, double[]> analysis) throws Exception {
		try {
			// Create the instance from the data
			Instance instance = instance(analysis.getInput(), instances);
			// Filter from string to inverse vector if necessary
			if (filter != null) {
				filter.input(instance);
				instance = filter.output();
			}
			// Classify the instance
			double classification = classifier.classifyInstance(instance);

			// Set the output for the client
			String clazz = instances.classAttribute().value((int) classification);
			double[] output = classifier.distributionForInstance(instance);
			analysis.setClazz(clazz);
			analysis.setOutput(output);
			analysis.setAlgorithmOutput(classifier.toString());

			return analysis;
		} catch (Exception e) {
			LOGGER.error("Exception classifying content : " + analysis.getInput(), e);
			throw new RuntimeException(e);
		} finally {
			// Clear the instances every so often to avoid an out of memory
			if (instances.numInstances() > 1000) {
				instances.delete();
			}
		}
	}
	
	private void log(final Instances instances) throws Exception {
		if (instances != null) {
			int numClasses = instances.numClasses();
			int numAttributes = instances.numAttributes();
			int numInstances = instances.numInstances();
			LOGGER.info("Building classifier, classes : " + numClasses + ", attributes : " + numAttributes + ", instances : " + numInstances);
			Evaluation evaluation = new Evaluation(instances);
			evaluation.evaluateModel(classifier, instances);
			String evaluationReport = evaluation.toSummaryString();
			LOGGER.info("Classifier evaluation : " + evaluationReport);
		}
	}

}