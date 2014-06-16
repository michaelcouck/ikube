package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class WekaTest extends AbstractTest {

	@Test
	public void readArff() throws Exception {
		WekaClassifier wekaClassifier = new WekaClassifier();

		File file = FileUtilities.findFileRecursively(new File("."), "classification.arff");
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		Instances instances = new Instances(bufferedReader);

		Classifier classifier = new SMO();
		Filter filter = new StringToWordVector();

		instances.setClassIndex(0);
		filter.setInputFormat(instances);
		Instances filteredData = Filter.useFilter(instances, filter);

		classifier.buildClassifier(filteredData);

		Instance instance = wekaClassifier.instance(IConstants.NEGATIVE, instances);
		filter.input(instance);
		Instance filteredInstance = filter.output();

		double result = classifier.classifyInstance(filteredInstance);
		String classificationClass = instances.classAttribute().value((int) result);

		assertEquals(IConstants.NEGATIVE, classificationClass);
	}

	@Test
	public void createProgrammatically() throws Exception {
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

		assertEquals(IConstants.POSITIVE, classificationClass);
	}

	@Test
	public void regression() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "regression.arff");

		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		Instances instances = new Instances(bufferedReader);

		Classifier classifier = new SimpleLinearRegression();
		instances.setClassIndex(5);

		classifier.buildClassifier(instances);

		Instance instance = instances.instance(0);

		double result = classifier.classifyInstance(instance);
		String classificationClass = instances.classAttribute().value((int) result);
		System.out.println("Result : " + result + ", " + classificationClass);

		Context<WekaClassifier, ?, SimpleLinearRegression, ?> context = new Context<>();
		context.setAlgorithm(SimpleLinearRegression.class.newInstance());
		// context.setFilter(StringToWordVector.class.newInstance());
		context.setName("regression");
		context.setMaxTraining(10000);

		WekaClassifier wekaClassifier = new WekaClassifier() {
			void persist(final Context context, final Instances instances) {
				// Do nothing
			}
		};
		wekaClassifier.init(context);
		wekaClassifier.build(context);

		Double[][] inputs = new Double[][] {
				{ 205000d, 3529d, 9191d, 6d, 0d, 0d },
				{ 224900d, 3247d, 10061d, 5d, 1d, 1d },
				{ 197900d, 4032d, 10150d, 5d, 0d, 1d },
				{ 189900d, 2397d, 14156d, 4d, 1d, 0d },
				{ 195000d, 2200d, 9600d, 4d, 0d, 1d },
				{ 325000d, 3536d, 19994d, 6d, 1d, 1d },
				{ 230000d, 2983d, 9365d, 5d, 0d, 1d }
		};

		for (final Double[] input : inputs) {
			Analysis<Object, Object> analysis = getAnalysis(input);
			analysis = wekaClassifier.analyze(analysis);
			//System.out.println("Output : " + Arrays.deepToString((Object[]) analysis.getOutput()));
            double[] outputs = (double[]) analysis.getOutput();
            System.out.println("Class : " + analysis.getClazz());
            System.out.println("Class : " + Arrays.toString(analysis.getClassesOrClusters()));
            for (final double output : outputs) {
                System.out.println("Output : " + output);
            }
        }

	}

	protected Analysis<Object, Object> getAnalysis(final Double[] input) {
		Analysis<Object, Object> analysis = new Analysis<>();
		analysis.setInput(input);
		analysis.setAlgorithm(Boolean.TRUE);
		analysis.setClassesAndClusters(Boolean.TRUE);
		analysis.setCorrelation(Boolean.TRUE);
		analysis.setDistribution(Boolean.TRUE);
		return analysis;
	}

}