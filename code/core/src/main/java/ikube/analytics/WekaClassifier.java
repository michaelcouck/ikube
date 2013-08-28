package ikube.analytics;

import java.util.Arrays;

import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier implements IClassifier<String, String, Object, Object> {

	public WekaClassifier() throws Exception {
		// Declare two numeric attributes
		Attribute firstNumeric = new Attribute("firstNumeric");
		Attribute secondNumeric = new Attribute("secondNumeric");

		// Declare a nominal attribute along with its values
		FastVector colourVector = new FastVector(3);
		colourVector.addElement("blue");
		colourVector.addElement("gray");
		colourVector.addElement("black");
		Attribute aNominal = new Attribute("aNominal", colourVector);

		// Declare the class attribute along with its values
		FastVector binomialVector = new FastVector(2);
		binomialVector.addElement("positive");
		binomialVector.addElement("negative");
		Attribute klass = new Attribute("theClass", binomialVector);

		// Declare the feature vector
		FastVector aggregateVector = new FastVector(4);
		aggregateVector.addElement(firstNumeric);
		aggregateVector.addElement(secondNumeric);
		aggregateVector.addElement(aNominal);
		aggregateVector.addElement(klass);

		// Create an empty training set
		Instances trainingInstance = new Instances("Rel", aggregateVector, 10);
		// Set class index
		trainingInstance.setClassIndex(3);

		// Create the instance
		Instance instance = new Instance(4);
		instance.setValue((Attribute) aggregateVector.elementAt(0), 1.0);
		instance.setValue((Attribute) aggregateVector.elementAt(1), 0.5);
		instance.setValue((Attribute) aggregateVector.elementAt(2), "gray");
		instance.setValue((Attribute) aggregateVector.elementAt(3), "positive");

		// add the instance
		trainingInstance.add(instance);

		// Create a classifier
		Classifier classifier = new SMO(); // new NaiveBayes();
		classifier.buildClassifier(trainingInstance);

		// Test the model
		Evaluation evaluation = new Evaluation(trainingInstance);
		evaluation.evaluateModel(classifier, trainingInstance);

		// Print the result à la Weka explorer:
		String strSummary = evaluation.toSummaryString();
		System.out.println(strSummary);

		// Get the confusion matrix
		double[][] cmMatrix = evaluation.confusionMatrix();
		System.out.println(Arrays.deepToString(cmMatrix));

		FastVector testVector = new FastVector(1);
		testVector.addElement(aNominal);
		Instances testInstances = new Instances("Rel", testVector, 1);
		Instance testInstance = new Instance(1);
		testInstance.setDataset(testInstances);
		double classification = classifier.classifyInstance(testInstance);
		System.out.println("Classification : " + classification);

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

	@Test
	public void classify() throws Exception {
		WekaClassifier wekaClassifier = new WekaClassifier();
	}

}