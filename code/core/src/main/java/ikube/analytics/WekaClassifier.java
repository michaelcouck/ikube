package ikube.analytics;

import ikube.IConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * TODO Document me...
 * 
 * @author Michael Couck
 * @since 14.08.13
 * @version 01.00
 */
public class WekaClassifier implements IClassifier<String, String, String, Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private Filter filter;
	private Classifier classifier;
	private Instances trainingInstances;
	private Instances classificationInstances;

	private int buildThreshold = 1000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		classifier = new SMO();
		filter = new StringToWordVector();

		// The general attributes for the instances, contains the string
		// attribute for the input text and the class attributes for the output
		FastVector attributes = new FastVector(2);
		// The class attributes, i.e. positive and negative
		FastVector classValues = new FastVector(2);
		classValues.addElement(IConstants.POSITIVE);
		classValues.addElement(IConstants.NEGATIVE);

		// Add the input text attribute
		attributes.addElement(new Attribute(IConstants.TEXT, (FastVector) null));
		// Add the class attributes for the output classification
		attributes.addElement(new Attribute(IConstants.CLASS, classValues));

		trainingInstances = new Instances("Training Instance", attributes, 100);
		trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String classify(final String input) {
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
			LOGGER.error(null, e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Boolean train(final String clazz, final String trainingInput) {
		try {
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
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
		return Boolean.FALSE;
	}

	/**
	 * TODO Document me...
	 * 
	 * @throws Exception
	 */
	public void build() throws Exception {
		int numClasses = trainingInstances.numClasses();
		int numAttributes = trainingInstances.numAttributes();
		int numInstances = trainingInstances.numInstances();
		LOGGER.info("Building classifier : " + numClasses + ", " + numAttributes + ", " + numInstances);

		filter.setInputFormat(trainingInstances);
		Instances filteredData = Filter.useFilter(trainingInstances, filter);
		Classifier classifier = Classifier.makeCopy(this.classifier);
		classifier.buildClassifier(filteredData);
		this.classifier = classifier;
		classificationInstances = trainingInstances.stringFreeStructure();

		// Evaluation evaluation = new Evaluation(trainingInstances);
		// LOGGER.info("Classifier evaluation : " + evaluation.toClassDetailsString());
		// IOUtils.writeInstancesToArffFile(filteredData, this.getClass().getSimpleName() + ".arff");
	}

	/**
	 * TODO Document me...
	 * 
	 * @param text
	 * @param instances
	 * @return
	 */
	private Instance makeInstance(final String text, final Instances instances) {
		// Create instance of length two.
		SparseInstance instance = new SparseInstance(2);
		// Set value for message attribute
		Attribute messageAtt = instances.attribute(IConstants.TEXT);
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		// Give instance access to attribute information from the dataset.
		instance.setDataset(instances);
		return instance;
	}

}