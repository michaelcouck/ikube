package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class WekaTest extends AbstractTest {

	@Test
	public void arff() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "instance.arff");
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		Instances instances = new Instances(bufferedReader);
		logger.info(instances.toString());
		Classifier classifier = new SMO();
		classifier.buildClassifier(instances);
		
		Instance instance = makeInstance("hello world", instances);
	}
	
	/**
	 * TODO Document me...
	 * 
	 * @param text
	 * @param instances
	 * @return
	 */
	private synchronized Instance makeInstance(final String text, final Instances instances) {
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
