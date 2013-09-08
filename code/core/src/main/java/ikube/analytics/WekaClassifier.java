package ikube.analytics;

import java.util.Arrays;

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

public class WekaClassifier implements IClassifier<String, String, Object, Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private Filter filter;
	private Classifier classifier;
	private Instances trainingInstances;
	private Instances classificationInstances;

	@Override
	public String classify(final String input) {
		try {
			classificationInstances = trainingInstances.stringFreeStructure();
			Instance instance = makeInstance(input, classificationInstances);
			filter.input(instance);
			Instance filteredInstance = filter.output();

			double[] result = classifier.distributionForInstance(filteredInstance);
			double classification = classifier.classifyInstance(filteredInstance);

			LOGGER.info("Result : " + classification + ", " + Arrays.toString(result));
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
		return null;
	}

	@Override
	public Object train(final Object trainingInput) {
		try {
			filter = new StringToWordVector();
			classifier = new SMO();
			FastVector attributes = new FastVector(2);
			attributes.addElement(new Attribute("text", (FastVector) null));
			FastVector classValues = new FastVector(2);
			classValues.addElement("true");
			classValues.addElement("false");
			attributes.addElement(new Attribute("class", classValues));

			trainingInstances = new Instances("Classification", attributes, 100);
			trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);

			// Make message into instance.
			Instance instance = makeInstance("positive", trainingInstances);
			// Set class value for instance.
			instance.setClassValue("true");
			// Add instance to training data.
			trainingInstances.add(instance);

			// And another training instance
			instance = makeInstance("negative", trainingInstances);
			// Set class value for instance.
			instance.setClassValue("false");
			// Add instance to training data.
			trainingInstances.add(instance);

			filter.setInputFormat(trainingInstances);
			Instances filteredData = Filter.useFilter(trainingInstances, filter);
			classifier.buildClassifier(filteredData);
			IOUtils.writeInstancesToArffFile(filteredData, this.getClass().getSimpleName() + ".arff");
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
		return null;
	}

	private Instance makeInstance(String text, Instances instances) {
		// Create instance of length two.
		SparseInstance instance = new SparseInstance(2);
		// Set value for message attribute
		Attribute messageAtt = instances.attribute("text");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		// Give instance access to attribute information from the dataset.
		instance.setDataset(instances);
		return instance;
	}

}