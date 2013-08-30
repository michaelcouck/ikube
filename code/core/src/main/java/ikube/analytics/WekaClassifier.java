package ikube.analytics;

// import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier implements IClassifier<String, String, Object, Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private Classifier classifier;
	private Instance instance;
	private Instances instances;

	@Override
	public String classify(String input) {
		return null;
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

	// @Test
	public void train() throws Exception {
		new WekaClassifier().train("Michael Couck");
	}

}
