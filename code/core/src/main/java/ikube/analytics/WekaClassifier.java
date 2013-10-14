package ikube.analytics;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * This class is a classifier for sentiment essentially, i.e. positive/negative. This classifier is based on the {@link SMO} classification algorithm from
 * mWeka, which is a support vector classifier.
 * 
 * @author Michael Couck
 * @since 14.08.13
 * @version 01.00
 */
public class WekaClassifier implements IAnalyzer<String, String, String, Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	/** This is the filter that will convert the text to tf-idf vectors. */
	private Filter filter;
	/** The classifier that will be built from time to time. */
	private volatile Classifier classifier;
	/** The instances that are used to train the classifier, i.e. these are pre-analyzed and correct with respect to the class attribute. */
	private Instances trainingInstances;
	/** The instances that is used to add the instance to for classification. */
	private volatile Instances classificationInstances;
	/** The number of vectors to keep in the training data set before we re-build the classifier. */
	private int buildThreshold = 1000;
	/** The file that contains the initial training data and instances. */
	private String filePattern;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		classifier = new SMO();
		filter = new StringToWordVector();
		if (filePattern != null) {
			try {
				File file = FileUtilities.findFileRecursively(new File("."), filePattern);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				trainingInstances = new Instances(bufferedReader);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		} else {
			// The general attributes for the instances, contains the string
			// attribute for the input text and the class attributes for the output
			FastVector attributes = new FastVector(2);
			// The class attributes, i.e. positive and negative
			FastVector classValues = new FastVector(2);
			classValues.addElement(IConstants.POSITIVE);
			classValues.addElement(IConstants.NEGATIVE);

			// Add the class attributes for the output classification
			attributes.addElement(new Attribute(IConstants.CLASS_ATTRIBUTE, classValues));
			// Add the input text attribute
			attributes.addElement(new Attribute(IConstants.TEXT_ATTRIBUTE, (FastVector) null));

			trainingInstances = new Instances("Training Instance", attributes, 100);
		}
		trainingInstances.setClassIndex(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String analyze(final String input) {
		try {
			// Create the instance with the text
			Instance instance = makeInstance(input, classificationInstances);
			// Filter the text from a string to a bag of words and finally a vector which is the TFIDF
			// (http://en.wikipedia.org/wiki/Tf%E2%80%93idf) or the inverse document frequency, or the number of
			// times the word appears in the text relative to the number of times it appears in the rest of the corpus
			filter.input(instance);
			Instance filteredInstance = filter.output();

			// Get the more 'likely' class for the vector distribution
			double classification = classifier.classifyInstance(filteredInstance);
			String classificationClass = classificationInstances.classAttribute().value((int) classification);

			// This is not really necessary because we get the classification and not a distribution
			// double[] result = classifier.distributionForInstance(filteredInstance);
			// LOGGER.info("Result : " + classificationClass + ", " + classification + ", " + Arrays.toString(result));
			return classificationClass;
		} catch (Exception e) {
			LOGGER.error("Exception classifying content : " + input, e);
		} finally {
			classificationInstances.delete();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Boolean train(final String clazz, final String trainingInput) {
		try {
			if (IConstants.POSITIVE.equals(clazz) || IConstants.NEGATIVE.equals(clazz)) {
				// Make message into instance.
				Instance instance = makeInstance(trainingInput, trainingInstances);
				// Set class value for instance.
				instance.setClassValue(clazz);
				// Add instance to training data.
				trainingInstances.add(instance);
				// If we reach the threshold for the vectors in the training corpus then
				// we rebuild the classifier, which can be expensive of course, but not very
				if (trainingInstances.numInstances() > 0 && trainingInstances.numInstances() % buildThreshold == 0) {
					build();
				}
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			LOGGER.error("Exception creating a training instance : ", e);
		}
		return Boolean.FALSE;
	}

	/**
	 * This method will build the classifier again using the training instances. When the training instances get to a certain number we can re-build the
	 * classifier from the training data. We catch all exceptions and clean the training instance data set of all the instances that are a problem.
	 */
	public synchronized void build() {
		try {
			int numClasses = trainingInstances.numClasses();
			int numAttributes = trainingInstances.numAttributes();
			int numInstances = trainingInstances.numInstances();
			LOGGER.info("Building classifier, classes : " + numClasses + ", attributes : " + numAttributes + ", instances : " + numInstances);
			// LOGGER.info("Training instances : " + trainingInstances.toString());

			filter.setInputFormat(trainingInstances);
			Instances filteredData = Filter.useFilter(trainingInstances, filter);
			Classifier classifier = new SMO();
			classifier.buildClassifier(filteredData);
			this.classifier = classifier;
			// We take a copy of the training instances, although we don't really have to I guess
			classificationInstances = trainingInstances.stringFreeStructure();

			Evaluation evaluation = new Evaluation(filteredData);
			evaluation.evaluateModel(classifier, filteredData);
			String evaluationReport = evaluation.toSummaryString();
			LOGGER.info("Classifier evaluation : " + evaluationReport);
			
			filteredData.setRelationName("filtered_data");
			trainingInstances.setRelationName("training_data");
			classificationInstances.setRelationName("classification_data");
			// writeInstances(trainingInstances, classificationInstances, filteredData);
			return;
		} catch (Exception e) {
			LOGGER.info("Exception building classifier : ", e);
		}
		// If we get here then there was and exception so we clean the
		// training instances of the last batch of vectors as this was obviously the problem
		trainingInstances.delete();
		classificationInstances.delete();
	}
	
	void writeInstances(final Instances... instances) throws IOException {
		for (final Instances i : instances) {
			IOUtils.writeInstancesToArffFile(i, i.relationName() + ".arff");
		}
	}

	/**
	 * This method will create one instance of data, i.e. a single vector from the text provided and add it to the instances for classification.
	 * 
	 * @param text the text to create a vector from for classification
	 * @param instances the instances data set that will hold the instance for classification
	 * @return the instance with the text data as a vector
	 */
	synchronized Instance makeInstance(final String text, final Instances instances) {
		// Create instance of length two, the first attribute is the class and the second is the vector from the text
		SparseInstance instance = new SparseInstance(2);
		// Set value for message attribute
		Attribute messageAtt = instances.attribute(IConstants.TEXT_ATTRIBUTE);
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		// Give instance access to attribute information from the dataset
		instance.setDataset(instances);
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void file(final String filePattern) {
		this.filePattern = filePattern;
	}

}