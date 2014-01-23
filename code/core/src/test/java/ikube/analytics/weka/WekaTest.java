package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

}