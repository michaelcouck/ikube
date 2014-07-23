package ikube.analytics;

import ikube.model.Context;

import java.io.Serializable;

/**
 * This is the API for classes that wrap analysis algorithms. Any algorithms that are used in the system
 * need to conform to this interface, so that the analytics logic can use any algorithm underneath.
 *
 * @param <I> the input type
 * @param <O> the output type
 * @param <C> the clazz input variable, in the case of the {@link ikube.analytics.weka.WekaClassifier} it is an
 *            {@link ikube.model.Analysis} object, with the {@link ikube.model.Analysis#clazz} attribute set to the
 *            required class count, for example 'positive'
 * @author Michael Couck
 * @version 01.00
 * @since 14-08-2013
 */
public interface IAnalyzer<I, O, C> extends Serializable {

    /**
     * This method will initialize the analyzer, but not build it. Any components that need to be
     * constructed should be done here, like the {@link weka.classifiers.functions.SMO} algorithm under
     * the hood for example.
     *
     * @param context the context that will be used for the construction of the analyzer, holds all
     *                the pieces that are required to construct the components
     * @throws Exception
     */
    void init(final Context context) throws Exception;

    /**
     * This method trains a particular algorithm, if it supports training, or incremental training.
     *
     * @param context the context of the component that will be trained
     * @param input   the input for the training, in the case of the Weka analyzer wrappers this class
     *                is one of {@link ikube.model.Analysis}, with the class or cluster specified and the
     *                data for training in the input
     * @return whether the training was successful. If the algorithm underneath does not support
     * incremental training then the return <ill be false
     * @throws Exception
     */
    boolean train(final Context context, final I input) throws Exception;

    /**
     * Builds the analyzer, creating the model that will be used for the analysis. This method can be
     * potentially long, and as the {@link ikube.model.Context} can contain several analyzers(homogeneous)
     * this method should build the analyzers in the context in parallel.
     *
     * @param context the context to use for building the analyzers, and of course the models
     * @throws Exception
     */
    void build(final Context context) throws Exception;

    /**
     * Does the analysis on the built analyzers. Note that init and build need to be called before
     * any analysis can be performed. This method may involve parallel processing, and indeed because the
     * {@link ikube.model.Context} is multi-analyzer there may need to be extra logic to aggregate the
     * results of multiple analyzers, perhaps some kind of {@link weka.classifiers.meta.Vote} logic, as
     * in the case of the Weka analyzers.
     *
     * @param context the context of the component that will be analyzed
     * @param input   the data to be analyzed, in the case of the Weka analyzers it is
     *                an  {@link ikube.model.Analysis} object, but need not be
     * @return the result of the analysis, could be anything, a probability, the {@link ikube.model.Analysis}
     * object that wraps the result, this depends on the underlying algorithm being used
     * @throws Exception
     */
    O analyze(final Context context, final I input) throws Exception;

    /**
     * This method will get the sizes for the class or cluster that is passed in the parameter list as
     * a property of the {@link C} or input object. In the case of a clusterer this will be for example
     * 1000 instances in cluster one, and in the case of a classifier 1000 'positive' values in the
     * data set/corpus.
     *
     * @param context the context for the analyzer. Contexts bind the components of the analyzer, like the algorithm,
     *                the data set and the filter. Including the properties and a data file or data source
     * @param clazz   the class object or an object that contains the class that should be used for the analysis
     * @return the number of instances in the data set that satisfy the class value specified
     * @throws Exception
     */
    int sizeForClassOrCluster(final Context context, final C clazz) throws Exception;

    /**
     * Destroys the analyzer, removing all data from memory. Typically this will be called in a M2M
     * environment, where analyzers are created and destroyed rapidly.
     *
     * @param context the context of the analyzer to destroy
     * @throws Exception
     */
    void destroy(final Context context) throws Exception;

}