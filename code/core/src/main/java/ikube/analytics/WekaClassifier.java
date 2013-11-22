package ikube.analytics;

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

	public static final int BUILD_THRESHOLD = 1000;

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private Filter filter;
	private Instances trainingInstances;

	private volatile Classifier classifier;
	private volatile Instances classificationInstances;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	public void init(final Buildable buildable) throws Exception {
		classifier = (Classifier) Class.forName(buildable.getType()).newInstance();
		trainingInstances = instances(buildable);
		trainingInstances.setClassIndex(0);
		if (!StringUtils.isEmpty(buildable.getFilter())) {
			filter = (Filter) Class.forName(buildable.getFilter()).newInstance();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean train(final String... strings) {
		try {
			Instance instance = instance(strings[1], trainingInstances);
			instance.setClassValue(strings[0]);
			trainingInstances.add(instance);
			if (trainingInstances.numInstances() > 0 && trainingInstances.numInstances() % BUILD_THRESHOLD == 0) {
				build(null);
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			LOGGER.error("Exception creating a training instance : ", e);
			throw new RuntimeException(e);
		}
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
				filteredData = trainingInstances;
			} else {
				filter.setInputFormat(trainingInstances);
				filteredData = Filter.useFilter(trainingInstances, filter);
			}

			Classifier classifier = new SMO();
			classifier.buildClassifier(filteredData);
			this.classifier = classifier;
			classificationInstances = trainingInstances.stringFreeStructure();

			filteredData.setRelationName("filtered_data");
			trainingInstances.setRelationName("training_data");
			log(filteredData);
			return;
		} catch (Exception e) {
			LOGGER.info("Exception building classifier : ", e);
			trainingInstances.delete();
			classificationInstances.delete();
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String analyze(final String input) {
		try {
			double classification = -1;
			Instance instance = instance(input, classificationInstances);
			if (filter != null) {
				filter.input(instance);
				Instance filteredInstance = filter.output();
				classification = classifier.classifyInstance(filteredInstance);
			} else {
				classification = classifier.classifyInstance(instance);
			}
			return classificationInstances.classAttribute().value((int) classification);
		} catch (Exception e) {
			LOGGER.error("Exception classifying content : " + input, e);
			throw new RuntimeException(e);
		} finally {
			if (classificationInstances.numInstances() > BUILD_THRESHOLD) {
				classificationInstances.delete();
			}
		}
	}

	private void log(final Instances instances) throws Exception {
		int numClasses = trainingInstances.numClasses();
		int numAttributes = trainingInstances.numAttributes();
		int numInstances = trainingInstances.numInstances();
		LOGGER.info("Building classifier, classes : " + numClasses + ", attributes : " + numAttributes + ", instances : " + numInstances);
		if (instances != null) {
			Evaluation evaluation = new Evaluation(instances);
			evaluation.evaluateModel(classifier, instances);
			String evaluationReport = evaluation.toSummaryString();
			LOGGER.info("Classifier evaluation : " + evaluationReport);
		}
	}

}