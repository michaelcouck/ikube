package ikube.analytics;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier implements IClassifier<String, String, Object, Object> {

	public WekaClassifier() throws Exception {
		// Declare two numeric attributes
		Attribute Attribute1 = new Attribute("firstNumeric");
		Attribute Attribute2 = new Attribute("secondNumeric");

		// Declare a nominal attribute along with its values
		FastVector fvNominalVal = new FastVector(3);
		fvNominalVal.addElement("blue");
		fvNominalVal.addElement("gray");
		fvNominalVal.addElement("black");
		Attribute Attribute3 = new Attribute("aNominal", fvNominalVal);

		// Declare the class attribute along with its values
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("positive");
		fvClassVal.addElement("negative");
		Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

		// Declare the feature vector
		FastVector fvWekaAttributes = new FastVector(4);
		fvWekaAttributes.addElement(Attribute1);
		fvWekaAttributes.addElement(Attribute2);
		fvWekaAttributes.addElement(Attribute3);
		fvWekaAttributes.addElement(ClassAttribute);

		// Create an empty training set
		Instances isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
		// Set class index
		isTrainingSet.setClassIndex(3);

		// Create the instance
		Instance iExample = new Instance(4);
		iExample.setValue((Attribute) fvWekaAttributes.elementAt(0), 1.0);
		iExample.setValue((Attribute) fvWekaAttributes.elementAt(1), 0.5);
		iExample.setValue((Attribute) fvWekaAttributes.elementAt(2), "gray");
		iExample.setValue((Attribute) fvWekaAttributes.elementAt(3), "positive");

		// add the instance
		isTrainingSet.add(iExample);

		// Create a naïve bayes classifier
		Classifier cModel = (Classifier) new NaiveBayes();
		cModel.buildClassifier(isTrainingSet);

		// Test the model
		Evaluation eTest = new Evaluation(isTrainingSet);
		eTest.evaluateModel(cModel, isTrainingSet);

		// Print the result à la Weka explorer:
		String strSummary = eTest.toSummaryString();
		System.out.println(strSummary);

		// Get the confusion matrix
		double[][] cmMatrix = eTest.confusionMatrix();

		// Specify that the instance belong to the training set
		// in order to inherit from the set description
		// iUse.setDataset(isTrainingSet);

		// Get the likelihood of each classes
		// fDistribution[0] is the probability of being “positive”
		// fDistribution[1] is the probability of being “negative”
		// double[] fDistribution = cModel.distributionForInstance(iUse);
	}

	@Override
	public String classify(String input) {
		return null;
	}

	@Override
	public Object train(Object trainingInput) {
		return null;
	}

}