package ikube.analytics;

/**
 * TODO Document me...
 *
 * @param <I> the input type
 * @param <O> the output type
 * @author Michael Couck
 * @version 01.00
 * @since 14.08.13
 */
public interface IAnalyzer<I, O> {

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
         * Ths maximum number of instances that can be used to train this analyzer.
         *
         * @return maximum training instances
         */
        int getMaxTraining();
    }

    void init(final IContext context) throws Exception;

    boolean train(final I input) throws Exception;

    void build(final IContext context) throws Exception;

    O analyze(final I input) throws Exception;

}