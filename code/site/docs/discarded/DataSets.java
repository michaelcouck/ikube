package ikube.analytics;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Timer;

import java.io.File;
import java.io.StringReader;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public final class DataSets {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataSets.class);

	@Test
	public void dataSet() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "classification.arff");
		String content = FileUtilities.getContent(file);
		StringReader reader = new StringReader(content);
		ArffReader arffReader = new ArffReader(reader);
		Instances instances = arffReader.getData();
		instances.setClassIndex(0);

		final StringToWordVector stringToWordVector = new StringToWordVector();
		stringToWordVector.setIDFTransform(Boolean.TRUE);
		stringToWordVector.setInputFormat(instances);

		instances = Filter.useFilter(instances, stringToWordVector);

		Classifier classifier = new SMO();
		classifier.buildClassifier(instances);

		Instance lastInstance = instances.lastInstance();

		final FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(stringToWordVector);
		filteredClassifier.setClassifier(classifier);

		filteredClassifier.buildClassifier(instances);

		final SparseInstance instance = new SparseInstance(lastInstance.numAttributes());
		instance.setDataset(instances);
		instances.add(instance);
		
		classifyMessage(instances, stringToWordVector, filteredClassifier, "network");
		
		// int[] indices = new int[] { 15, 44, 47, 48, 76, 107, 123, 130, 167, 214, 216, 257, 270, 303, 326, 329, 349, 414, 416, 421, 431, 445, 456, 457, 466,
		// 486, 489 };
		// double[] values = new double[] { 5.298317366548036, 2.8134107167600364, 5.298317366548036, 5.298317366548036, 4.605170185988092, 5.298317366548036,
		// 5.298317366548036, 4.199705077879927, 5.298317366548036, 4.605170185988092, 4.605170185988092, 1.448169764837978, 4.605170185988092,
		// 5.298317366548036, 5.298317366548036, 4.605170185988092, 1.9310215365615626, 4.605170185988092, 5.298317366548036, 3.6888794541139363,
		// 2.407945608651872, 4.605170185988092, 5.298317366548036, 1.0642108619507773, 4.199705077879927, 4.605170185988092, 5.298317366548036 };
		// for (int i = 0; i < indices.length; i++) {
		// instance.setValueSparse(indices[i], values[i]);
		// }

		long duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {
				double result;
				try {
					stringToWordVector.input(instance);
					Instance textInstance = stringToWordVector.output();
					result = filteredClassifier.classifyInstance(textInstance);
					LOGGER.info("Result : " + result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		LOGGER.info("Duration : " + duration);
	}

	public double[] classifyMessage(Instances instances, StringToWordVector filter, Classifier classifier, String message) throws Exception {
		message = message.toLowerCase();
		Instances testset = instances.stringFreeStructure();
		Instance testInstance = makeInstance(message, testset);
		// Filter instance.
		filter.input(testInstance);
		Instance filteredInstance = filter.output();
		return classifier.distributionForInstance(filteredInstance);
	}

	private Instance makeInstance(String text, Instances data) {
		// Create instance of length two.
		Instance instance = new Instance(2);
		// Set value for message attribute
		Attribute messageAtt = data.attribute("text");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}

}