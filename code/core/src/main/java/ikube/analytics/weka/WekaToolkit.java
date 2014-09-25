package ikube.analytics.weka;

import ikube.toolkit.CsvUtilities;
import ikube.toolkit.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

import static ikube.toolkit.FileUtilities.getOrCreateFile;
import static ikube.toolkit.MatrixUtilities.objectVectorToDoubleVector;
import static ikube.toolkit.MatrixUtilities.objectVectorToStringVector;

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
    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
     * This is a convenience method to load a pure csv file and create an instances object that Weka
     * can use. This would typically be used in an environment where there are missing values in the input
     * data and the Weka loader doesn't like that.
     *
     * @param filePath   the path to the file to load the data from
     * @param classIndex the class index that is missing, can be -1 if there is no known missing attribute,
     *                   and the missing attribute will be set to 0, i.e. the first one, which is the default
     * @return the instances object created from the input, data, with the same number of attributed labeled as
     * the input data has vector lengths
     */
    public static Instances csvFileToInstances(final String filePath, final int classIndex, final Class<?> type) {
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            Object[][] matrix = CsvUtilities.getCsvData(inputStream);
            return matrixToInstances(matrix, classIndex, type);
        } catch (final IOException e) {
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
     * @param type       the type of attributes to use in the instances, either double or string
     * @return the instances object created from the matrix, with all the attributes doubles or strings, ready for processing
     */
    public static Instances matrixToInstances(final Object[][] matrix, final int classIndex, final Class<?> type) {
        // Create the instances from the matrix data
        ArrayList<Attribute> attributes = new ArrayList<>();
        // Add the attributes to the data set
        for (int i = 0; i < matrix[0].length; i++) {
            attributes.add(getAttribute(i, type));
        }
        // Create the instances data set from the data and the attributes
        Instances instances = new Instances("instances", attributes, 0);
        instances.setClass(instances.attribute(classIndex));
        // Populate the instances
        for (final Object[] vector : matrix) {
            instances.add(getInstance(instances, vector, type));
        }
        return instances;
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
        if (Double.class.isAssignableFrom(type)) {
            return new Attribute(name);
        } else if (String.class.isAssignableFrom(type)) {
            return new Attribute(name, (List<String>) null);
        } else if (Date.class.isAssignableFrom(type)) {
            return new Attribute(name, DATE_FORMAT);
        } else if (nominal != null && nominal.length > 0) {
            List<String> nominalAttributes = Arrays.asList(nominal);
            return new Attribute(name, nominalAttributes);
        } else {
            throw new RuntimeException("Attribute type not supported : " + type);
        }
    }

    public static Instance getInstance(final Instances instances, final Object[] vector, final Class<?> type) {
        if (Double.class.isAssignableFrom(type)) {
            double[] doubleVector = objectVectorToDoubleVector(vector);
            return new DenseInstance(1.0, doubleVector);
        } else if (String.class.isAssignableFrom(type)) {
            String[] stringVector = objectVectorToStringVector(vector);
            Instance instance = new DenseInstance(stringVector.length);
            for (int i = 0; i < instances.numAttributes(); i++) {
                Attribute attribute = instances.attribute(i);
                String value = stringVector[i];
                instance.setValue(attribute, value);
            }
            return instance;
        } else {
            throw new RuntimeException("Attribute type not supported : " + type);
        }
    }

    public static Instance getInstance(final Instances instances, final Object[] vector) throws ParseException {
        Instance instance = new DenseInstance(vector.length);
        instance.setDataset(instances);
        for (int i = 0; i < instances.numAttributes(); i++) {
            String value = vector[i] == null ? "" : vector[i].toString();
            Attribute attribute = instances.attribute(i);
            // LOGGER.error("Index : " + i + ", " + attribute);
            switch (attribute.type()) {
                case Attribute.DATE: {
                    instance.setValue(i, attribute.parseDate(value));
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
     * @param filters  the filter to use for the transformation
     * @return the filtered instance that is usable in the analyzer
     * @throws Exception
     */
    public static Instance filter(final Instance instance, final Filter... filters) throws Exception {
        if (filters == null || filters.length == 0) {
            return instance;
        }
        Instance filteredInstance = instance;
        for (final Filter filter : filters) {
            // Filter from string to inverse vector if necessary
            if (filter != null) {
                filter.input(filteredInstance);
                filteredInstance = filter.output();
            }
        }
        return filteredInstance;
    }

    /**
     * This method applies multiple filters on the {@link weka.core.Instances} object, hopefully
     * replacing missing values, converting to numbers and so on.
     *
     * @param instances the instances data set to apply the filters on, converting and returning a new instances object potentially
     * @param filters   the filters to be applied to the data in the instances object
     * @return a filtered instances, potentially a new instance
     * @throws Exception
     */
    public static synchronized Instances filter(final Instances instances, final Filter... filters) throws Exception {
        if (filters == null || filters.length == 0) {
            return instances;
        }
        Instances filteredInstances = instances;
        for (final Filter filter : filters) {
            if (filter != null) {
                filter.setInputFormat(filteredInstances);
                filteredInstances = Filter.useFilter(filteredInstances, filter);
            }
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
                    // new Range(), true
                    evaluation.crossValidateModel(classifier, instances, folds, new Random(), predictionsOutput);
                    LOGGER.error(predictionsOutput.globalInfo());
                    LOGGER.error(predictionsOutput.getDisplay());
                } catch (final Exception e) {
                    LOGGER.error("Exception cross validating the classifier : ", e);
                }
            }
        });
        LOGGER.warn("Duration for cross validation : " + duration);

        duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    evaluation.evaluateModel(classifier, instances);
                } catch (final Exception e) {
                    LOGGER.error("Exception evaluating the classifier : ", e);
                }
            }
        });
        LOGGER.warn("Duration for model evaluation : " + duration);

        return evaluation.relativeAbsoluteError();
    }

    private WekaToolkit() {
    }

}
