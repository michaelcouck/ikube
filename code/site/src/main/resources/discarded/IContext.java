package ikube.analytics;

/**
 * TODO Document me...
 *
 * @param <T> the type of analyzer in Ikube system
 * @param <F> the type of the filter to convert the data to the input format
 * @param <A> the logical implementation or algorithm for the analyzer
 * @author Michael Couck
 * @version 01.00
 * @since 28-01-14
 */
public interface IContext<T, F, A> {

    /**
     * The name of this specific analyzer. The name will also be used to find the initial training file,
     * and indeed persist the instances that were used to train this analyzer for further investigation and modification.
     *
     * @return the name of the analyzer, must be unique in the system
     */
    String getName();

    /**
     * Ths internal type, i.e. the {@link ikube.analytics.IAnalyzer} type. Could be for instance
     * a {@link ikube.analytics.weka.WekaClassifier}. This class then holds a reference to the logical implementation,
     * and in the case of a classifier this could be a {@link weka.classifiers.functions.SMO} function.
     * <p/>
     * This typically is only defined using the interface, if the analyzers are defined in Spring,then we know immediately
     * what the type will be of course.
     *
     * @return the type of analyzer in the Ikube system
     */
    T getAnalyzer();

    /**
     * The filter type to convert the data into for example feature vectors.
     *
     * @return the filter for the data, can be null
     */
    F getFilter();

    /**
     * The underlying algorithm for the analyzer, for example KMeans or J48 for example.
     *
     * @return the underlying logical algorithm
     */
    A getAlgorithm();

    /**
     * This method returns the string training data, typically set from the front end.
     *
     * @return the Weka format training data for the analyzer
     */
    String getTrainingData();

    /**
     * Ths maximum number of instances that can be used to train this analyzer.
     *
     * @return maximum training instances
     */
    int getMaxTraining();
}
