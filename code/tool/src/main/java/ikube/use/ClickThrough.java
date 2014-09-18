package ikube.use;

import ikube.analytics.weka.WekaToolkit;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;

import java.io.File;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
public class ClickThrough {

    private Context[] contexts;

    @SuppressWarnings("unchecked")
    public void hillClimb() throws Exception {
        // Load the data from the file
        // Test several algorithms, varying the parameters
        // Retest the algorithms, modifying the instances, reducing the vector space, etc.
        File file = FileUtilities.findFileRecursively(new File("."), "click-through.csv");
        String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());

        Classifier classifier = new LinearRegression();
        Instances instances = WekaToolkit.csvToInstances(filePath, 0);
        classifier.buildClassifier(instances);


    }

}
