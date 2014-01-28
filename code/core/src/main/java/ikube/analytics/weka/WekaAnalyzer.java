package ikube.analytics.weka;

import ikube.IConstants;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;

import java.io.*;
import java.util.Enumeration;

/**
 * TODO Document me...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18.11.13
 */
public abstract class WekaAnalyzer implements IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * This method will create an instance from the input string. The string is assumed to be a comma separated list of values, with the same dimensions as the
     * attributes in the instances data set. If not, then the results are undefined.
     *
     * @param string the input string, a comma separated list of values, i.e. '35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES'
     * @return the instance, with the attributes set to the values of the tokens in the input string
     */
    Instance instance(final String string, final Instances instances) {
        String[] values = StringUtils.split(string, ',');
        Instance instance = new Instance(instances.numAttributes());
        instance.setMissing(0);
        for (int i = instances.numAttributes() - 1, j = values.length - 1; i >= 1 && j >= 0; i--, j--) {
            String value = values[j];
            Attribute attribute = instances.attribute(i);
            if (!attribute.isString()) {
                instance.setValue(attribute, value);
            } else {
                instance.setValue(attribute, attribute.addStringValue(value));
            }
        }
        instance.setDataset(instances);
        return instance;
    }

    /**
     * This method is for accessing the training/structure file and instantiating an {@link Instances} object.
     *
     * @param context the configuration object to build the instances object from
     * @return the instances object built from the arff training and structure file
     * @throws IOException
     */
    Instances instances(final Context context) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream(context);
            Reader reader = new InputStreamReader(inputStream);
            return new Instances(reader);
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    /**
     * This method persists the instances data to the Weka format file. This file can then be used to
     * train the classifier in the future, or for inspection.
     *
     * @param context   the analyzer context for holding the configuration details
     * @param instances the instances to persist to a file
     */
    void persist(final Context context, final Instances instances) {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                File file = getDataFile(context);
                if (file != null) {
                    logger.info("Persisting data : " + file);
                    String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());
                    WekaToolkit.writeToArff(instances, filePath);
                }
            }
        });
        logger.info("Persisted data in : " + duration);
    }

    InputStream getInputStream(final Context context) throws FileNotFoundException {
        if (context.getTrainingData() != null) {
            return new ByteArrayInputStream(context.getTrainingData().getBytes());
        }
        return new FileInputStream(getDataFile(context));
    }

    File getDataFile(final Context context) {
        String name = context.getName();
        File directory = FileUtilities.findDirectoryRecursively(new File(IConstants.ANALYTICS_DIRECTORY), name);
        File file = new File(directory, name + ".arff");
        if (!file.exists() || !file.canRead()) {
            logger.info("Can't find data file : " + file + ", will search for it...");
            String fileName = FilenameUtils.getName(file.getName());
            File dataFile = FileUtilities.findFileRecursively(new File("."), fileName);
            if (dataFile == null || !dataFile.exists() || !dataFile.canRead()) {
                logger.info("Couldn't find file for analyzer or can't read file, will create it : " + fileName);
                file = FileUtilities.getOrCreateFile(file);
                if (file != null) {
                    logger.info("Found data file : " + file.getAbsolutePath());
                }
            }
        }
        return file;
    }

    Instance filter(final Instance instance, final Filter filter) throws Exception {
        // Filter from string to inverse vector if necessary
        Instance filteredData;
        if (filter == null) {
            filteredData = instance;
        } else {
            filter.input(instance);
            filteredData = filter.output();
        }
        return filteredData;
    }

    Instances filter(final Instances instances, final Filter filter) throws Exception {
        Instances filteredData;
        if (filter == null) {
            filteredData = instances;
        } else {
            filter.setInputFormat(instances);
            filteredData = Filter.useFilter(instances, filter);
        }
        return filteredData;
    }

    double[][] getCorrelationCoefficients(final Instances instances) {
        int numAttributes = instances.numAttributes();
        double[][] correlationCoefficients = new double[numAttributes][numAttributes];
        for (int i = 0; i < numAttributes; i++) {
            double[] attributeValuesArrayI = instances.attributeToDoubleArray(i);
            for (int j = 0; j < i; j++) {
                double[] attributeValuesArrayJ = instances.attributeToDoubleArray(j);
                double correlationCoefficient = Utils.correlation(attributeValuesArrayI, attributeValuesArrayJ, numAttributes);
                correlationCoefficients[i][j] = correlationCoefficient;
            }
        }
        return correlationCoefficients;
    }

    @SuppressWarnings("unchecked")
    double[][] getDistributionForInstances(final Instances instances) throws Exception {
        double[][] distributionForInstances = new double[instances.numInstances()][];
        if (instances.numInstances() > 0) {
            Enumeration<Instance> enumeration = instances.enumerateInstances();
            int index = 0;
            do {
                Instance instance = enumeration.nextElement();
                double cluster = classOrCluster(instance);
                double greatest = 0;
                double[] distribution = distributionForInstance(instance);
                for (final double probability : distribution) {
                    if (Math.abs(probability) > Math.abs(greatest)) {
                        greatest = probability;
                    }
                }
                double[] distributionForInstance = new double[2];
                distributionForInstance[0] = cluster;
                distributionForInstance[1] = greatest;
                distributionForInstances[index] = distributionForInstance;
                index++;
            } while (enumeration.hasMoreElements());
        }
        return distributionForInstances;
    }

    abstract double classOrCluster(final Instance instance) throws Exception;

    abstract double[] distributionForInstance(final Instance instance) throws Exception;

}