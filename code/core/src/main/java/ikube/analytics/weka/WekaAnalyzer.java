package ikube.analytics.weka;

import com.google.common.collect.Lists;
import ikube.analytics.AAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

import static ikube.analytics.weka.WekaToolkit.filter;
import static ikube.toolkit.ThreadUtilities.submit;
import static ikube.toolkit.ThreadUtilities.waitForAnonymousFutures;

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
    public void init(final Context context) throws Exception {
        if (String.class.isAssignableFrom(context.getAnalyzer().getClass())) {
            context.setAnalyzer(Class.forName(context.getAnalyzer().toString()).newInstance());
        }
        Object[] algorithms = context.getAlgorithms();
        Object[] filters = context.getFilters();
        // Create the analyzer algorithm, the filter and the model
        for (int i = 0; i < algorithms.length; i++) {
            Object algorithm = Class.forName(algorithms[i].toString()).newInstance();
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
        context.setModels(instances(context));
    }

    @Override
    public void build(final Context context) throws Exception {

        final Object[] algorithms = context.getAlgorithms();
        final Object[] models = context.getModels();

        final String[] evaluations = new String[algorithms.length];
        final Object[] capabilities = new Object[algorithms.length];

        class AnalyzerBuilder implements Runnable {
            final int index;

            AnalyzerBuilder(final int index) {
                this.index = index;
            }

            public void run() {
                try {
                    Instances instances = (Instances) models[index];
                    // Filter filter = getFilter(context, index);
                    Filter[] filters = (Filter[]) context.getFilters();

                    // Filter the data if necessary
                    Instances filteredInstances = filter(instances, filters);
                    filteredInstances.setRelationName("filtered-instances");

                    Object analyzer = algorithms[index];
                    if (Clusterer.class.isAssignableFrom(analyzer.getClass())) {
                        Clusterer clusterer = ((Clusterer) analyzer);
                        logger.info("Building clusterer : " + instances.numInstances());
                        clusterer.buildClusterer(filteredInstances);
                        evaluations[index] = WekaToolkit.evaluate(clusterer, instances);
                        capabilities[index] = clusterer.getCapabilities().toString();
                        logger.info("Clusterer built : " + filteredInstances.numInstances() + ", " + context.getName());
                    } else if (Classifier.class.isAssignableFrom(analyzer.getClass())) {
                        Classifier classifier = (Classifier) analyzer;
                        // And build the model
                        logger.error("Building classifier : " + instances.numInstances() + ", " + context.getName());
                        classifier.buildClassifier(filteredInstances);
                        logger.error("Classifier built : " + filteredInstances.numInstances() + ", " + context.getName());
                        // Set the evaluation of the classifier and the training model
                        evaluations[index] = WekaToolkit.evaluate(classifier, filteredInstances);
                        capabilities[index] = classifier.getCapabilities().toString();
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        List<Future> futures = Lists.newArrayList();
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Future<?> future = submit(this.getClass().getName(), new AnalyzerBuilder(i));
            futures.add(future);
        }
        waitForAnonymousFutures(futures, Long.MAX_VALUE);

        context.setAlgorithms(algorithms);
        context.setEvaluations(evaluations);
        context.setCapabilities(capabilities);
        context.setBuilt(Boolean.TRUE);
    }

    /**
     * This method returns the distribution for the instance. The distribution is the probability that the instance
     * falls into either the classification or cluster category, and suggests the classification or cluster of the instance.
     *
     * @param context  the context for the analyzer, holds all the components, the classifier, the model and the options etc.
     * @param instance the instance to get the distribution for
     * @return the probability distribution for the instance over the classesOrClusters or clusters
     * @throws Exception
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
        Object[] values = null;
        if (String.class.isAssignableFrom(input.getClass())) {
            values = StringUtils.split((String) input, ',');
        } else if (input.getClass().isArray()) {
            values = (Object[]) input;
        }
        return WekaToolkit.getInstance(instances, values);
    }

    /**
     * This method is for accessing the training/structure file and instantiating an {@link Instances} object.
     *
     * @param context the configuration object to build the instances object from
     * @return the instances object built from the arff training and structure file
     * @throws IOException
     */
    Instances[] instances(final Context context) throws IOException {
        InputStream[] inputStreams = getInputStreams(context);
        Instances[] instances = new Instances[context.getAlgorithms().length];
        for (int i = 0; inputStreams != null && i < inputStreams.length; i++) {
            try {
                if (inputStreams[i] == null) {
                    continue;
                }
                InputStream inputStream = inputStreams[i];
                try {
                    Reader reader = new InputStreamReader(inputStream);
                    instances[i] = new Instances(reader);
                    instances[i].setRelationName("instances");
                } finally {
                    if (inputStream != null) {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
            } catch (final Exception e) {
                logger.error("Exception building analyzer : " + context.getName(), e);
                throw new RuntimeException(e);
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
     * @throws FileNotFoundException
     */
    InputStream[] getInputStreams(final Context context) throws FileNotFoundException {
        InputStream[] inputStreams = new InputStream[context.getAlgorithms().length];
        if (context.getTrainingDatas() != null) {
            for (int i = 0; i < context.getTrainingDatas().length; i++) {
                inputStreams[i] = new ByteArrayInputStream(context.getTrainingDatas()[i].getBytes());
            }
        } else {
            File[] dataFiles = getDataFiles(context);
            for (int i = 0; dataFiles != null && i < dataFiles.length; i++) {
                inputStreams[i] = new FileInputStream(dataFiles[i]);
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
     * @throws Exception
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

    Filter getFilter(final Context context, final int i) {
        Object[] filters = context.getFilters();
        Filter filter = null;
        if (filters != null && filters.length > i) {
            filter = (Filter) filters[i];
        }
        return filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(final Context context) throws Exception {
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            Instances instances = (Instances) context.getModels()[i];
            instances.delete();
        }
    }

}