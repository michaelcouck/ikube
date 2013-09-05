package ikube.analytics;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaClassifier implements IClassifier<String, String, Object, Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private Classifier classifier;
	private Instance instance;
	private Instances instances;

	@Override
	public String classify(String input) {
		Classifier classifier = null;
		try {
			FastVector attrInfo = new FastVector();
			attrInfo.addElement(new Attribute("text"));
			
			FastVector targetValues = new FastVector();
			targetValues.addElement("true");
			targetValues.addElement("false");
			Attribute target = new Attribute("target", targetValues);

			attrInfo.addElement(target);

			Instances wekaInstanceSet = new Instances("Dataset", attrInfo, 0);
			Instance wekaInstance = new Instance(2);
			wekaInstance.setDataset(wekaInstanceSet);
			wekaInstanceSet.add(wekaInstance);
			wekaInstance.setValue((Attribute) attrInfo.elementAt(0), 1);
			wekaInstance.setValue((Attribute) attrInfo.elementAt(1), 1);
			
			StringToWordVector stringToWordVector = new StringToWordVector();
			stringToWordVector.setIDFTransform(Boolean.TRUE);
			stringToWordVector.setInputFormat(wekaInstanceSet);
			stringToWordVector.input(wekaInstance);
			wekaInstanceSet = Filter.useFilter(wekaInstanceSet, stringToWordVector);
			
			wekaInstanceSet.setClassIndex(1);
			classifier = new SMO();
			// classifier = new J48();
			// classifier.setOptions(new String[] { "-R" });

			// Now add some training instances
			Instance i1 = new Instance(2);
			i1.setDataset(wekaInstanceSet);
			wekaInstanceSet.add(i1);
			i1.setValue((Attribute) attrInfo.elementAt(0), 2);
			i1.setValue((Attribute) attrInfo.elementAt(1), 1);
			
			Instance i2 = new Instance(2);
			i2.setDataset(wekaInstanceSet);
			wekaInstanceSet.add(i2);
			i2.setValue((Attribute) attrInfo.elementAt(0), 2);
			i2.setValue((Attribute) attrInfo.elementAt(1), 1);

			classifier.buildClassifier(wekaInstanceSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void classify() throws Exception {
		new WekaClassifier().classify("Michael Couck");
	}

	@Override
	public Object train(Object trainingInput) {
		classifier = new J48();

		Attribute one = new Attribute("one");
		Attribute two = new Attribute("two");
		Attribute three = new Attribute("three");
		Attribute four = new Attribute("four");

		FastVector attInfo = new FastVector();
		attInfo.addElement(one);
		attInfo.addElement(two);
		attInfo.addElement(three);
		attInfo.addElement(four);

		instances = new Instances("Instances", attInfo, 10);

		instances.setClassIndex(3);
		LOGGER.info(instances.attribute(instances.classIndex()).toString());

		try {
			double[] attValues = new double[instances.numAttributes()];
			attValues[0] = Instance.missingValue();
			attValues[1] = instances.attribute(1).indexOfValue("value_9");
			attValues[2] = instances.attribute(2).addStringValue("Marinka");
			attValues[3] = instances.attribute(3).addStringValue("23-4-1989");

			instance = new Instance(1.0, attValues);
			instance.setDataset(instances);

			instances.add(instance);

			classifier.buildClassifier(instances);
			Evaluation evaluation = new Evaluation(instances);
			evaluation.evaluateModel(classifier, instances);
			String strSummary = evaluation.toSummaryString();
			LOGGER.info("Summary : " + strSummary);
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
		return null;
	}

}
