package ikube.analytics.weka;

import ikube.IConstants;
import ikube.toolkit.STRING;
import ikube.toolkit.Timer;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static ikube.toolkit.FILE.getContents;
import static ikube.toolkit.FILE.getOrCreateFile;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.split;

/**
 * This class contains general methods for manipulating the Weka data, and for writing
 * the models to the file system.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public final class WekaToolkit {

    private static final Logger LOGGER = LoggerFactory.getLogger(WekaToolkit.class);

    /**
     * Writes the instances to a file that can be loaded again and used to train algorithms.
     *
     * @param instances the instances data to write to the file
     * @param filePath  the absolute path to the output file
     */
    public static void writeToArff(final Instances instances, final String filePath) {
        try {
            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(instances);
            File file = getOrCreateFile(filePath);
            arffSaver.setFile(file);
            arffSaver.writeBatch();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method converts and instances object to a sparse instance, which will then
     * be more memory efficient.
     *
     * @param instances the instances to convert to a sparse instances model
     * @return the sparse instances, more memory efficient
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Instances nonSparseToSparse(final Instances instances) {
        try {
            NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
            nonSparseToSparseInstance.setInputFormat(instances);
            return Filter.useFilter(instances, nonSparseToSparseInstance);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will convert a matrix to a 'flat' {@link weka.core.Instances} object. Note that the
     * matrix will be converted where necessary to a double vector to comply with the input requirements
     * of the Weka algorithms, or string depending on the type in the parameter list.
     *
     * @param matrix     the matrix to convert into an instances object, or data set for Weka
     * @param classIndex the class index or index o the attribute that is 'missing' or to be predicted, if this is
     *                   set to Integer.MAX_VALUE then the last attribute will be used as the class index
     * @return the instances object created from the matrix, with all the attributes doubles or strings, ready for processing
     */
    public static Instances matrixToInstances(final Object[][] matrix, final int classIndex) throws ParseException {
        // Create the instances from the matrix data
        ArrayList<Attribute> attributes = new ArrayList<>();
        // Add the attributes to the data set
        for (int i = 0; i < matrix[0].length; i++) {
            String value = matrix[0][i] == null ? "" : matrix[0][i].toString();
            if (STRING.isDate(value)) {
                attributes.add(getAttribute(i, Date.class));
            } else if (STRING.isNumeric(value)) {
                attributes.add(getAttribute(i, Double.class));
            } else if (value.startsWith("{") && value.endsWith("}")) {
                String strippedValue = remove(remove(value, '}'), '{');
                String[] nominalValues = split(strippedValue, IConstants.DELIMITER_CHARACTERS);
                attributes.add(getAttribute(i, String.class, nominalValues));
            } else {
                attributes.add(getAttribute(i, String.class));
            }
        }
        // Create the instances data set from the data and the attributes
        Instances instances = new Instances("instances", attributes, 0);
        instances.setClass(instances.attribute(classIndex));
        // Populate the instances
        for (final Object[] vector : matrix) {
            instances.add(getInstance(instances, vector));
        }
        return instances;
    }

    /**
     * This method converts a Json representation of a matrix, i.e. [[1,2,3], [4,5,6], [7,8,9]]...
     * to a real matrix of objects, using the Gson converter from Google.
     *
     * @param inputStream the Json matrix representation from the input stream to convert to the instances
     * @return the instances object from the string input
     */
    public static Instances csvToInstances(final InputStream inputStream) {
        try {
            String input = getContents(inputStream, Integer.MAX_VALUE).toString();
            String[] rows = split(input, "\n\r");
            Object[][] matrix = new Object[rows.length][];
            for (int i = 0; i < rows.length; i++) {
                String row = rows[i];
                matrix[i] = split(row, ',');
            }
            return matrixToInstances(matrix, 0);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will create an {@link weka.core.Instances} object from the input stream, which
     * must be an arff input stream.
     *
     * @param inputStream the arff input data to create the instances from
     * @return the instances data set from the arff data input
     */
    public static Instances arffToInstances(final InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream)) {
            return new Instances(reader);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will create an attribute that can be used in the Weka models, specifically
     * the {@link weka.core.Instances} objects.
     *
     * @param index   the index of the attribute in the {@link weka.core.Instances} object
     *                and the name of the attribute, any arbitrary name
     * @param type    the type of the attribute, numeric, nominal, string, date etc.
     * @param nominal the attributes/names that are to be applied to the nominal attribute
     * @return the attribute with the name and type specified
     */
    public static Attribute getAttribute(final int index, final Class<?> type, final String... nominal) {
        String name = Integer.toString(index);
        if (nominal != null && nominal.length > 0) {
            List<String> nominalAttributes = Arrays.asList(nominal);
            return new Attribute(name, nominalAttributes);
        } else if (Double.class.isAssignableFrom(type)) {
            return new Attribute(name);
        } else if (String.class.isAssignableFrom(type)) {
            return new Attribute(name, (List<String>) null);
        } else if (Date.class.isAssignableFrom(type)) {
            return new Attribute(name, IConstants.SHORT_DATE_FORMAT);
        } else {
            throw new RuntimeException("Attribute type not supported : " + type);
        }
    }

    /**
     * This method will convert the vector to an instance. The type of attributes are gotten from
     * the {@link weka.core.Instances} object.
     *
     * @param instances the data set to add this instance to, and to get the attribute types from
     * @param vector    the vector of values to convert to an instance
     * @return the instance object, with the instances set as the data set
     */
    public static Instance getInstance(final Instances instances, final Object[] vector) {
        int featureSpace = Math.max(instances.numAttributes(), vector.length);
        Instance instance = instances.numAttributes() == vector.length ? new DenseInstance(featureSpace) : new SparseInstance(featureSpace);
        instance.setDataset(instances);
        String[] dateFormats = new String[] {IConstants.SHORT_DATE_FORMAT, IConstants.ANALYTICS_DATE_FORMAT};
        //  && i < vector.length
        for (int i = instances.numAttributes() - 1, j = vector.length - 1; i >= 0 && j >= 0; i--, j--) {
            // System.out.println("I : " + i + ", j : " + j);
            String value = vector[j] == null ? "" : vector[j].toString();
            Attribute attribute = instances.attribute(i);
            switch (attribute.type()) {
                case Attribute.DATE: {
                    try {
                        double time = DateUtils.parseDate(value, dateFormats).getTime();
                        instance.setValue(i, time);
                    } catch (final ParseException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case Attribute.STRING:
                case Attribute.NOMINAL: {
                    instance.setValue(i, attribute.addStringValue(value));
                    break;
                }
                case Attribute.NUMERIC: {
                    instance.setValue(i, Double.parseDouble(value));
                    break;
                }
                default: {
                    throw new RuntimeException("Attribute type not supported : " + attribute.type());
                }
            }
        }
        return instance;
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
    public static Instance filter(final Instance instance, final Filter filter) throws Exception {
        Instance filteredInstance = instance;
        // Filter from string to inverse vector if necessary
        if (filter != null) {
            filter.input(instance);
            filteredInstance = filter.output();
        }
        return filteredInstance;
    }

    /**
     * This method applies multiple filters on the {@link weka.core.Instances} object, hopefully
     * replacing missing values, converting to numbers and so on.
     *
     * @param instances the instances data set to apply the filters on, converting and returning a new instances object potentially
     * @param filter    the filter to be applied to the data in the instances object
     * @return a filtered instances, potentially a new instance
     * @throws Exception
     */
    public static synchronized Instances filter(final Instances instances, final Filter filter) throws Exception {
        Instances filteredInstances = instances;
        if (filter != null) {
            filter.setInputFormat(filteredInstances);
            filteredInstances = Filter.useFilter(filteredInstances, filter);
        }
        return filteredInstances;
    }

    /**
     * This method will cross train a classifier using the number of folds, and then evaluate the model. Note
     * that cross validation is expensive, and with a million vectors can take several hours on a single thread.
     *
     * @param classifier the classifier to cross validate
     * @param instances  the instances to be used for the cross validation
     * @param folds      the number of folds to cross validate the model
     * @return the error rate of the cross validation
     * @throws Exception
     */
    @SuppressWarnings("UnusedDeclaration")
    public static double crossValidate(final Classifier classifier, final Instances instances, final int folds) throws Exception {
        final Evaluation evaluation = new Evaluation(instances);
        final PlainText predictionsOutput = new PlainText();
        predictionsOutput.setBuffer(new StringBuffer());
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    evaluation.crossValidateModel(classifier, instances, folds, new Random(), predictionsOutput);
                    LOGGER.warn(predictionsOutput.globalInfo());
                    LOGGER.warn(predictionsOutput.getDisplay());
                } catch (final Exception e) {
                    LOGGER.error("Exception cross validating the classifier : ", e);
                }
            }
        });
        LOGGER.warn("Duration for cross validation : " + duration);
        return evaluation.relativeAbsoluteError();
    }

    public static String evaluate(final Classifier classifier, final Instances instances) throws Exception {
        final Evaluation evaluation = new Evaluation(instances);
        evaluation.evaluateModel(classifier, instances);
        return evaluation.toSummaryString(true);
    }

    public static String evaluate(final Clusterer clusterer, final Instances instances) throws Exception {
        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(instances);
        return clusterEvaluation.clusterResultsToString();
    }

    private WekaToolkit() {
    }

}
