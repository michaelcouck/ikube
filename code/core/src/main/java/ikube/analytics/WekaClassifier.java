package ikube.analytics;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier implements IClassifier<String, String, Object, Object> {

	private Classifier classifier;
	private Instance instance;
	private Instances trainingInstances;

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
		trainingInstances = new Instances("Rel", aggregateVector, 10);
		// Set class index
		trainingInstances.setClassIndex(3);

		// Create the instance
		instance = new Instance(4);
		instance.setValue((Attribute) aggregateVector.elementAt(0), 1.0);
		instance.setValue((Attribute) aggregateVector.elementAt(1), 0.5);
		instance.setValue((Attribute) aggregateVector.elementAt(2), "gray");
		instance.setValue((Attribute) aggregateVector.elementAt(3), "positive");

		// add the instance
		trainingInstances.add(instance);

		// Create a classifier
		classifier = new SMO(); // new NaiveBayes();
		classifier.buildClassifier(trainingInstances);

		// Test the model
		Evaluation evaluation = new Evaluation(trainingInstances);
		// evaluation.evaluateModel(classifier, trainingInstances);

		// Print the result à la Weka explorer:
		String strSummary = evaluation.toSummaryString();
		// System.out.println(strSummary);

		// Get the confusion matrix
		double[][] cmMatrix = evaluation.confusionMatrix();
		// System.out.println(Arrays.deepToString(cmMatrix));

		// FastVector testVector = new FastVector(1);
		// testVector.addElement(aNominal);
		// Instances testInstances = new Instances("Rel", testVector, 1);
		// Instance testInstance = new Instance(1);
		// testInstance.setDataset(testInstances);
		// double classification = classifier.classifyInstance(testInstance);

		// Attribute latitude = new Attribute("latitude");
		// Attribute longitude = new Attribute("longitude");
		// Attribute carbonmonoxide = new Attribute("co");
		//
		// Instance inst_co = new Instance(100);
		// inst_co.setValue(latitude, 1.0);
		// inst_co.setValue(longitude, 20);
		// inst_co.setValue(carbonmonoxide, 200);
		// inst_co.setMissing(4);

		// double classification = classifier.classifyInstance(inst_co);
		// System.out.println("Classification : " + classification);

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

		Instance inst_co;

		// Create attributes to be used with classifiers
		// Test the model
		double result = -1;
		try {

			FastVector attributeList = new FastVector(2);

			Attribute latitude = new Attribute("latitude");
			Attribute longitude = new Attribute("longitude");
			Attribute carbonmonoxide = new Attribute("co");

			FastVector classVal = new FastVector();
			classVal.addElement("ClassA");
			classVal.addElement("ClassB");

			attributeList.addElement(latitude);
			attributeList.addElement(longitude);
			attributeList.addElement(carbonmonoxide);

			attributeList.addElement(new Attribute("@@class@@", classVal));

			Instances data = new Instances("TestInstances", attributeList, 0);

			// Create instances for each pollutant with attribute values latitude,
			// longitude and pollutant itself
			inst_co = new Instance(data.numAttributes());
			data.add(inst_co);

			// Set instance's values for the attributes "latitude", "longitude", and
			// "pollutant concentration"
			inst_co.setValue(latitude, 0.0);
			inst_co.setValue(longitude, 0.0);
			inst_co.setValue(carbonmonoxide, 0.0);
			// inst_co.setMissing(0);
			inst_co.setDataset(trainingInstances);

			// load classifier from file
			// Classifier cls_co = (Classifier) weka.core.SerializationHelper.read("/CO_J48Model.model");

			result = classifier.classifyInstance(inst_co);
			System.out.println("Result : " + result);

			instance.setDataset(trainingInstances);
			result = classifier.classifyInstance(instance);
			System.out.println("Result : " + result);

			return Double.toString(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public Object train(Object trainingInput) {
		return null;
	}

	// @Test
	public void classify() throws Exception {
		WekaClassifier wekaClassifier = new WekaClassifier();
		wekaClassifier.classify("Michael Couck");
	}

}