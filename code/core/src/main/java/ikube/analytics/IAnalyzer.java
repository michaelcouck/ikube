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
 * @since 14.08.13
 */
public interface IAnalyzer<I, O, C> {

    void init(final Context context) throws Exception;

    boolean train(final I input) throws Exception;

    void build(final Context context) throws Exception;

    O analyze(final I input) throws Exception;

    void destroy(final Context context) throws Exception;

    int sizeForClassOrCluster(final C clazz) throws Exception;

    Object[] classesOrClusters() throws Exception;

}