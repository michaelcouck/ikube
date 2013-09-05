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
		Classifier classifier = null;
		try {
			FastVector attrInfo = new FastVector();
			FastVector targetValues = new FastVector();
			targetValues.addElement("true");
			targetValues.addElement("false");
			Attribute target = new Attribute("target", targetValues);
			String[] features = { "one", "two" };

			for (String feature : features) {
				Attribute attribute = new Attribute(feature);
				attrInfo.addElement(attribute);
			}
			attrInfo.addElement(target);

			Instance wekaInstance = new Instance(3);
			Instances wekaInstanceSet = new Instances("Dataset", attrInfo, 0);
			wekaInstanceSet.add(wekaInstance);
			List<Long> featureValues = Arrays.asList(1l, 2l);
			for (int i = 0; i < featureValues.size(); i++) {
				if (featureValues.get(i) != null) {
					wekaInstance.setValue((Attribute) attrInfo.elementAt(i), featureValues.get(i));
				}
			}
			wekaInstanceSet.setClassIndex(attrInfo.size() - 1);
			classifier = new SMO();
			classifier = new J48();
			// classifier.setOptions(new String[] { "-R" });

			// Now add some training instances
			Instance i1 = new Instance(3);
			i1.setDataset(wekaInstanceSet);
			// i1.setValue((Attribute) attrInfo.elementAt(0), "hello");
			// i1.setValue((Attribute) attrInfo.elementAt(1), "java");
			// i1.setValue((Attribute)attrInfo.elementAt(2), "true");
			wekaInstanceSet.add(i1);

			classifier.buildClassifier(wekaInstanceSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
