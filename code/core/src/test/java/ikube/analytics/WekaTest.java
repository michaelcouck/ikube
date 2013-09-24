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
	public void readArff() throws Exception {
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
		FastVector attributes = new FastVector();

		attributes.addElement(new Attribute("att1"));
		attributes.addElement(new Attribute("att2"));
		attributes.addElement(new Attribute("att3"));
		attributes.addElement(new Attribute("att4"));

		FastVector nominalValues = new FastVector(2);
		nominalValues.addElement(IConstants.POSITIVE);
		nominalValues.addElement(IConstants.NEGATIVE);
		attributes.addElement(new Attribute("class", nominalValues));

		Instances dataSet = new Instances("ESDN", attributes, 0);
		dataSet.setClass(dataSet.attribute(dataSet.numAttributes() - 1));

		double[] values = new double[dataSet.numAttributes()];
		values[0] = 3;
		values[1] = 7;
		values[3] = 1;
		dataSet.add(new SparseInstance(1.0, values));

		values = new double[dataSet.numAttributes()];
		values[2] = 2;
		values[3] = 8;
		dataSet.add(new SparseInstance(1.0, values));

		Classifier classifier = new SMO();
		classifier.buildClassifier(dataSet);

		double classification = classifier.classifyInstance(dataSet.firstInstance());
		String classificationClass = dataSet.classAttribute().value((int) classification);
		logger.info("Classification class : " + classificationClass);
	}
	
}