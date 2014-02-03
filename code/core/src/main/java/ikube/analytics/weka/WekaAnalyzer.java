package ikube.analytics.weka;

import ikube.IConstants;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Timer;
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
import java.util.Arrays;

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

    /**
     * This method gets the input stream to the data file or alternatively creates an input stream from the input in
     * the context. Typically when the latter is the case the analyzer is being trained via the rest API.
     *
     * @param context the context for the analyzer
     * @return the input stream either to the data file for training or a stream from the input in the context
     * @throws FileNotFoundException
     */
    InputStream getInputStream(final Context context) throws FileNotFoundException {
        if (context.getTrainingData() != null) {
            return new ByteArrayInputStream(context.getTrainingData().getBytes());
        }
        return new FileInputStream(getDataFile(context));
    }

    /**
     * This method returns the data file for the analyzer. The data file contains the relational attribute set that can be
     * used to train the analyzer. As with all configuration, we expect this file to reside in the {@link IConstants#IKUBE_DIRECTORY}
     * some where.
     *
     * @param context the context for the analyzer that we want the input training file for
     * @return the data file for the analyzer, or null if no such file exists. Typically if the file is
     * not present then there is a serious problem and the analyzers will not work properly or at all, i.e.
     * the results are undefined
     */
    File getDataFile(final Context context) {
        String name = context.getName();
        String fileName = name + ".arff";
        Object ikubeConfigurationPathProperty = System.getProperty(IConstants.IKUBE_CONFIGURATION);
        File directory;
        if (ikubeConfigurationPathProperty == null) {
            directory = new File(IConstants.ANALYTICS_DIRECTORY);
        } else {
            directory = new File(ikubeConfigurationPathProperty.toString(), IConstants.ANALYTICS_DIRECTORY);
        }
        File file = FileUtilities.findFileRecursively(directory, fileName);
        logger.info("Looking for data file in directory : " + directory.getAbsolutePath());
        if (file == null || !file.exists() || !file.canRead()) {
            logger.info("Can't find data file : " + fileName + ", will search for it...");
            file = FileUtilities.findFileRecursively(new File("."), fileName);
            if (file == null || !file.exists() || !file.canRead()) {
                logger.warn("Couldn't find file for analyzer or can't read file, will create it : " + fileName);
                directory = FileUtilities.getOrCreateDirectory(new File(IConstants.ANALYTICS_DIRECTORY));
                file = FileUtilities.getOrCreateFile(new File(directory, fileName));
                if (file != null) {
                    logger.info("Created data file : " + file.getAbsolutePath());
                } else {
                    logger.warn("Couldn't create data file : " + fileName);
                }
            }
        }
        return file;
    }

    /**
     * This method will filter the instance using the filter defined. Ultimately the filter changes the input
     * instance into an instance that is useful for the analyzer. For example in the case of a SVM classifier, the
     * support vectors are exactly that, vectors of doubles. If we are trying to classify text, we need to change(filter)
     * the text from words to feature vectors, most likely using the tf-idf logic. The filter essentially does that
     * for us in this method.
     *
     * @param instance the instance to filter into the correct form for the analyser
     * @param filter   the filter to use for the transformation
     * @return the filtered instance that is usable in the analyzer
     * @throws Exception
     */
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

    /**
     * As with the {@link ikube.analytics.weka.WekaAnalyzer#filter(weka.core.Instance, weka.filters.Filter)} method, this method filters
     * the entire data set into something that is usable. Typically this is used in the training faze of the logic when the 'raw' data set
     * needs to be transformed into a matrix that can be used for training the analyzer.
     *
     * @param instances the instances that are to be transformed using the filter
     * @param filter    the filter to use for the transformation
     * @return the transformed instances object, ready to be used in training the classifier
     * @throws Exception
     */
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

    /**
     * This method returns the correlation co-efficients for each instance compared to the next instance. The correlation is
     * typically the Pearson's product moment correlation, which is the linear relationship between two variables, in this case
     * the variables are in fact feature vectors for the instances.
     *
     * @param instances the instances data set
     * @return the correlation co-efficients for each instance relative to the following instance
     * @throws Exception
     */
    double[] getCorrelationCoefficients(final Instances instances) throws Exception {
        double[] correlationCoefficients = new double[instances.numInstances()];
        Instance one = null;
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance two = instances.instance(i);
            if (one != null) {
                double[] d1 = distributionForInstance(one);
                double[] d2 = distributionForInstance(two);
                double correlationCoefficient = Utils.correlation(d1, d2, d1.length);
                if (logger.isDebugEnabled()) {
                    logger.info("Vector one : " + Arrays.toString(d1));
                    logger.info("Vector two : " + Arrays.toString(d2));
                    logger.info("Correlation : " + correlationCoefficient);
                }
                correlationCoefficients[i] = correlationCoefficient;
            } else {
                correlationCoefficients[i] = 1.0;
            }
            one = two;
        }
        return correlationCoefficients;
    }

    /**
     * This method will return the distribution for the entire data set. The distribution is the probability of the
     * variable being in the specific class or cluster in the data set.
     *
     * @param instances the data set of instances to get the distribution for, of the individual instances of course
     * @return the total distribution for all the instances in the data set
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    double[][] getDistributionForInstances(final Instances instances) throws Exception {
        double[][] distributionForInstances = new double[instances.numInstances()][];
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            double[] distributionForInstance = distributionForInstance(instance);
            distributionForInstances[i] = distributionForInstance;
        }
        return distributionForInstances;
    }

    /**
     * This returns the class or the number of the cluster number. In the case of a classifier it is the
     * index of the class attribute that this instance falls into, in the case of a clusterer it is the index
     * of the cluster.
     *
     * @param instance the instance to get the classification attribute index or cluster number for
     * @return the classification index or the cluster number for the instance
     * @throws Exception
     */
    abstract double classOrCluster(final Instance instance) throws Exception;

    /**
     * This method returns the distribution for the instance. The distribution is the probability that the instance
     * falls into either the classification or cluster category, and suggests the classification or cluster of the instance.
     *
     * @param instance the instance to get the distribution for
     * @return the probability distribution for the instance over the classes or clusters
     * @throws Exception
     */
    abstract double[] distributionForInstance(final Instance instance) throws Exception;

}