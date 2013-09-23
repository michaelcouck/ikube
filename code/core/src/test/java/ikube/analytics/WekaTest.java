package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Ignore;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class WekaTest extends AbstractTest {

	@Test
	@Ignore
	public void arff() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "instance.arff");
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		Instances instances = new Instances(bufferedReader);
		logger.info(instances.toString());

		Instance instance = new Instance(1.0, new double[] { 1, 2, 3, 4 });

		Classifier classifier = new SMO();
		classifier.buildClassifier(instances);
	}

	@Test
	public void build() throws Exception {
		FastVector classAttInfo = new FastVector(2);
		classAttInfo.addElement(IConstants.POSITIVE);
		classAttInfo.addElement(IConstants.NEGATIVE);

		FastVector attInfo = new FastVector(2);
		attInfo.addElement(new Attribute("@@class@@", classAttInfo));
		attInfo.addElement(new Attribute("@@vectors@@"));

		Instances instances = new Instances("instances", attInfo, 100000);
		instances.setClassIndex(0);

		double[] attValues = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		int[] indices = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		SparseInstance sparseInstance = new SparseInstance(1.0, attValues, indices, Integer.MAX_VALUE);

		instances.add(sparseInstance);

		Classifier classifier = new SMO();
		classifier.buildClassifier(instances);
	}

	/**
	 * TODO Document me...
	 * 
	 * @param text
	 * @param instances
	 * @return
	 */
	synchronized Instance makeInstance(final String text, final Instances instances) {
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
