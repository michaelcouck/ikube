package ikube.analytics;

import ikube.model.Context;

/**
 * TODO Document me...
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
public interface IAnalyzer<I, O, C> {

    void init(final Context context) throws Exception;

    boolean train(final Context context, final I input) throws Exception;

    void build(final Context context) throws Exception;

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

    void destroy(final Context context) throws Exception;

}