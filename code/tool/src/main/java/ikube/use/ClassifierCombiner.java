package ikube.use;

import ikube.analytics.weka.WekaToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static ikube.analytics.weka.WekaToolkit.*;
import static ikube.toolkit.CsvUtilities.getCsvData;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class ClassifierCombiner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassifierCombiner.class);

    @SuppressWarnings("UnnecessaryLocalVariable")
    public void combine() throws Exception {
        String filePath = "/home/laptop/Downloads/netflix/processed-data/mv-aggregated-1411559729692-100000.csv";
        Object[][] matrix = getCsvData(filePath);

        ArrayList<Attribute> attributes = new ArrayList<>();

        attributes.add(getAttribute(0, Double.class));
        attributes.add(getAttribute(1, Double.class));
        attributes.add(getAttribute(2, String.class)); // The cluster of the titles
        attributes.add(getAttribute(3, Double.class));
        attributes.add(getAttribute(4, Double.class));
        attributes.add(getAttribute(5, Date.class));

        Instances instances = new Instances("instances", attributes, 0);
        for (final Object[] vector : matrix) {
            instances.add(getInstance(instances, vector));
        }
        instances.setClassIndex(4);

        Instances filteredInstances = filter(instances, new StringToWordVector());
        writeToArff(filteredInstances, "target/mv-aggregated-filtered.arff");

        Classifier classifier = new RegressionByDiscretization();
        classifier.buildClassifier(filteredInstances);
        double error = WekaToolkit.crossValidate(classifier, filteredInstances, 10);
        LOGGER.error("Error : " + error);

//        for (int i = 0; i < filteredInstances.numInstances(); i++) {
//            Instance instance = filteredInstances.instance(i);
//            double classIndex = classifier.classifyInstance(instance);
//            double[] distributionForInstance = classifier.distributionForInstance(instance);
//            LOGGER.error("Class index : " + classIndex + ", " + Arrays.toString(distributionForInstance));
//        }
    }

}