package ikube.analytics.weka;

import com.google.common.collect.Lists;
import ikube.analytics.AAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.filters.Filter;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;

import static ikube.analytics.weka.WekaToolkit.*;
import static ikube.toolkit.FILE.getContents;
import static ikube.toolkit.THREAD.*;
import static org.apache.commons.lang.StringUtils.split;

/**
 * This is the base class for the Weka implementation of the analytics API. It has base methods for creating
 * {@link weka.core.Instances} objects, loading data from files in 'arff' format, getting the class or cluster for an
 * instance, creating {@link weka.core.Instance} objects and filtering them for processing. There are also methods for
 * getting the correlation co-efficients and distributions for instances. {@link ikube.analytics.IAnalyzer} implementations
 * that wrap Weka should use this class as a base.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-11-2013
 */
public abstract class WekaAnalyzer extends AAnalyzer<Analysis, Analysis, Analysis> {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void init(final Context context) throws Exception {
        logger.warn("Analyzer context : " + context.getName() + " : " + context.hashCode());
        if (String.class.isAssignableFrom(context.getAnalyzer().getClass())) {
            context.setAnalyzer(Class.forName(context.getAnalyzer().toString()).newInstance());
        }
        Object[] algorithms = context.getAlgorithms();
        Object[] filters = context.getFilters();
        // Create the analyzer algorithm, the filter and the model
        for (int i = 0; algorithms != null && i < algorithms.length; i++) {
            Object algorithm = algorithms[i];
            String algorithmClass;
            logger.warn("Building analyzer : " + algorithm.getClass());
            logger.warn("                  : " + algorithm.toString());
            if (algorithm.getClass().getName().equals(String.class.getName())) {
                algorithmClass = (String) algorithm;
            } else {
                algorithmClass = algorithm.getClass().getName();
            }
            algorithm = Class.forName(algorithmClass).newInstance();
            algorithms[i] = algorithm;
            if (filters != null && filters.length > i) {
                Filter filter = (Filter) Class.forName(filters[i].toString()).newInstance();
                filters[i] = filter;
            }
            Object[] options = context.getOptions();
            // The options for Weka are string arrays only
            if (options != null && options.length > 0 && OptionHandler.class.isAssignableFrom(algorithm.getClass())) {
                String[] cloned = new String[options.length];
                for (int j = 0; j < options.length; j++) {
                    cloned[j] = options[j].toString();
                }
                ((OptionHandler) algorithm).setOptions(cloned);
            }
        }
        // Load the models(Instances) for all the analyzers
        //noinspection ConfusingArgumentToVarargsMethod
        context.setModels(instances(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final Context context) throws Exception {
        final Object[] algorithms = context.getAlgorithms();
        final Object[] models = context.getModels();
        final Filter[] filters = getFilters(context);

        List<Future> futures = Lists.newArrayList();
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Filter filter = null;
            if (filters != null && filters.length > i) {
                filter = filters[i];
            }
            Runnable builder = getAnalyzerBuilder(algorithms[i], (Instances) models[i], filter);
            if (logger != null) {
                logger.info("Analyzer : " + i + ":" + builder);
            }
            Future<?> future = submit(this.getClass().getName(), builder);
            if (context.isBuildInParallel()) {
                futures.add(future);
            } else {
                waitForFuture(future, Long.MAX_VALUE);
            }
        }

        if (logger != null) {
            logger.info("Waiting for futures : ");
        }
        waitForAnonymousFutures(futures, Long.MAX_VALUE);

        context.setBuilt(Boolean.TRUE);
    }

    private Runnable getAnalyzerBuilder(final Object analyzer, final Instances instances, final Filter filter) {
        class AnalyzerBuilder implements Runnable {
            public void run() {
                try {
                    // Filter the data if necessary
                    Instances filteredInstances = filter(instances, filter);
                    filteredInstances.setRelationName("filtered-instances");

                    if (Clusterer.class.isAssignableFrom(analyzer.getClass())) {
                        Clusterer clusterer = ((Clusterer) analyzer);
                        logger.info("Building clusterer : " + instances.numInstances());
                        clusterer.buildClusterer(filteredInstances);
                        // evaluations[index] = evaluate(clusterer, instances);
                        // capabilities[index] = clusterer.getCapabilities().toString();
                        logger.info("Clusterer built : " + filteredInstances.numInstances());
                    } else if (Classifier.class.isAssignableFrom(analyzer.getClass())) {
                        Classifier classifier = (Classifier) analyzer;
                        // And build the model
                        logger.info("Building classifier : " + instances.numInstances());
                        classifier.buildClassifier(filteredInstances);
                        logger.info("Classifier built : " + filteredInstances.numInstances());
                        // Set the evaluation of the classifier and the training model
                        // evaluations[index] = evaluate(classifier, filteredInstances);
                        evaluate(classifier, filteredInstances);
                        // capabilities[index] = classifier.getCapabilities().toString();
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new AnalyzerBuilder();
    }

    /**
     * This method returns the distribution for the instance. The distribution is the probability that the instance
     * falls into either the classification or cluster category, and suggests the classification or cluster of the instance.
     *
     * @param context  the context for the analyzer, holds all the components, the classifier, the model and the options etc.
     * @param instance the instance to get the distribution for
     * @return the probability distribution for the instance over the classesOrClusters or clusters
     * @throws Exception anything and everything, let the caller handle if necessary
     */
    abstract double[][] distributionForInstance(final Context context, final Instance instance) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeForClassOrCluster(final Context context, final Analysis analysis) throws Exception {
        int sizeForClassOrCluster = 0;
        Instances[] models = (Instances[]) context.getModels();
        for (final Instances instances : models) {
            Attribute classAttribute = instances.classAttribute();
            Enumeration enumeration = instances.enumerateInstances();
            while (enumeration.hasMoreElements()) {
                Instance instance = (Instance) enumeration.nextElement();
                int classAttributeIndex = (int) instance.value(classAttribute.index());
                Object value = classAttribute.value(classAttributeIndex);
                if (analysis.getClazz().equals(value.toString())) {
                    sizeForClassOrCluster++;
                }
            }
        }
        return sizeForClassOrCluster;
    }

    /**
     * This method will create an instance from the input string. The string is assumed to be a comma separated list of values,
     * with the same dimensions as the attributes in the instances data set. If not, then the results are undefined.
     *
     * @param input the input string, a comma separated list of values, i.e. '35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES'
     * @return the instance, with the attributes set to the values of the tokens in the input string
     */
    Instance instance(final Object input, final Instances instances) {
        return getInstance(instances, inputToArray(input));
    }

    /**
     * Converts the input object, either already an array, or s string, or if not either of these then
     * to string, into an array that can be processed by the analyzers.
     *
     * @param input the input to convert to an array or vector for the analyzers
     * @return the array from the input object, typically either a string like '[1,2,3,...]'
     */
    private Object[] inputToArray(final Object input) {
        if (input == null) {
            return null;
        }
        Class<?> inputClass = input.getClass();
        if (String.class.isAssignableFrom(inputClass)) {
            return split((String) input, ',');
        } else if (inputClass.isArray()) {
            return (Object[]) input;
        } else {
            // Should we throw the toys here?
            return split(input.toString(), ',');
        }
    }

    /**
     * This method is for accessing the training/structure file and instantiating an {@link Instances} object.
     *
     * @param context the configuration object to build the instances object from
     * @return the instances object built from the arff training and structure file
     * @throws IOException anything and everything, let the caller handle if necessary
     */
    Instances[] instances(final Context context) throws IOException {
        InputStream[] inputStreams = getInputStreams(context);
        if (inputStreams == null) {
            return null;
        }
        Instances[] instances = new Instances[inputStreams.length];
        for (int i = 0; i < inputStreams.length; i++) {
            if (inputStreams[i] == null) {
                logger.warn("Input stream for instances null : ");
                continue;
            }
            int read = 1024;
            inputStreams[i].mark(0);
            String headers = getContents(inputStreams[i], read, false).toString();
            inputStreams[i].reset();
            if (headers.startsWith("@relation")) {
                instances[i] = arffToInstances(inputStreams[i]);
            } else {
                instances[i] = csvToInstances(inputStreams[i]);
            }
        }
        return instances;
    }

    /**
     * This method gets the input stream to the data file or alternatively creates an input stream from the input in
     * the context. Typically when the latter is the case the analyzer is being trained via the rest API.
     *
     * @param context the context for the analyzer
     * @return the input stream either to the data file for training or a stream from the input in the context
     * @throws FileNotFoundException if the training data is not found, configuration error
     */
    InputStream[] getInputStreams(final Context context) throws FileNotFoundException {
        InputStream[] inputStreams = null;
        if (context.getTrainingDatas() != null) {
            inputStreams = new InputStream[context.getTrainingDatas().length];
            for (int i = 0; i < context.getTrainingDatas().length; i++) {
                String trainingData = context.getTrainingDatas()[i];
                inputStreams[i] = new ByteArrayInputStream(trainingData.getBytes());
            }
        } else if (context.getFileNames() != null) {
            inputStreams = new InputStream[context.getFileNames().length];
            File[] dataFiles = getDataFiles(context);
            for (int i = 0; dataFiles != null && i < dataFiles.length; i++) {
                FileInputStream fileInputStream = new FileInputStream(dataFiles[i]);
                inputStreams[i] = new BufferedInputStream(fileInputStream);
                logger.info("Data file : " + dataFiles[i] + ", length : " + dataFiles[i].length());
            }
        }
        return inputStreams;
    }

    /**
     * This method will return the distribution for the entire data set. The distribution is the probability of the
     * variable being in the specific class or cluster in the data set.
     *
     * @param instances the data set of instances to get the distribution for, of the individual instances of course
     * @return the total distribution for all the instances in the data set
     * @throws Exception anything and everything, let the caller handle if necessary
     */
    @SuppressWarnings("unchecked")
    double[][][] getDistributionForInstances(final Context context, final Instances instances) throws Exception {
        double[][][] distributionForInstances = new double[instances.numInstances()][][];
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            double[][] distributionForInstance = distributionForInstance(context, instance);
            distributionForInstances[i] = distributionForInstance;
        }
        return distributionForInstances;
    }

    Filter[] getFilters(final Context context) {
        Filter[] filters;
        if (context.getFilters() == null) {
            filters = new Filter[0];
        } else {
            filters = new Filter[context.getFilters().length];
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(context.getFilters(), 0, filters, 0, filters.length);
        }
        return filters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(final Context context) throws Exception {
        if (context.getAlgorithms() != null) {
            for (int i = 0; context.getModels() != null && i < context.getAlgorithms().length; i++) {
                ((Instances) context.getModels()[i]).delete();
            }
        }
    }

}