package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Context;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

/**
 * This is the interface for all analytics operations. analytics operations generally consist of creating
 * analyzers, training them, building the models, and using them to perform some kind of analysis on, prediction
 * using regression, or classification using support vectors. Finally analyzers can also be destroyed, releasing
 * resources where necessary, analyzers being typically memory intensive objects.
 *
 * @author Michael Couck
 * @version 01.00
 * @see ikube.analytics.IAnalyzer for details on the parameterized types
 * @since 10-04-2013
 */
@Service
public interface IAnalyticsService<I, O> {

    /**
     * This method will create and analyzer, based on the context passed. The context typically
     * holds all the details that are required to create the analyzer, like the name of the algorithm,
     * the data file or matrix data etc.
     *
     * @param context the context with the details to build the analyzer
     * @return the context, with the analyzer attached and the models initialized, not necessarily built,
     * but at least initialized
     */
    Context create(final Context context);

    /**
     * This method will upload data and persist it in a file with the name specified. If this file does
     * not exist then it will be created, otherwise it will be over written. Typically this will be written in the
     * ikube directory in the base of the server, i.e. ./ikube/analytics/file-name.suffix
     *
     * @param fileName    the name of the file to persist the data to, overwritten if it exists
     * @param inputStream the input stream to persist in a file
     * @return whether the operation was successful
     */
    boolean upload(final String fileName, final InputStream inputStream);

    /**
     * This will return a portion of the data specified for the particular context. In the case that
     * there are multiple matrices, one for each model, then the returned data will be an array of matrices.
     *
     * @param context the context to return a portion of the data for
     * @param rows    the number of rows to return for each matrix
     * @return an array of matrices, one for each model defined for the context
     */
    Object[][][] data(final Context context, final int rows);

    /**
     * This method will train an analyzer, typically this means adding a vector to the matrix that is used to
     * build the model for the analyzer. This method does not build the model. The added vector will not have an
     * effect on the results of the analyses until after the model is built with the new data.
     *
     * @param analysis the analysis object, containing the extra vector to be added to the model matrix
     * @return the context that was associated with the analysis object, typically the name fo the context/analyzer
     */
    Context train(final Analysis<I, O> analysis);

    /**
     * This method will build the model, probably using the analyzer in some way to facilitate this. This is
     * typically a very heavy operation, taking a long time. Once the analyzer(s) are built, the context will have
     * a flag set to indicate this, and they will be ready to perform analysis operations.
     *
     * @param analysis the analysis object to use to invoke the build on the context and the analyzer
     * @return the context associated with the analysis object passed
     */
    Context build(final Analysis<I, O> analysis);

    /**
     * This method will take the data in the analysis object and perform ana analysis on it. This can be a prediction
     * or a classification, or in fact a clustering of the analysis input data. The type of input data that can be fed to
     * the analyzer depends on the way in which the analyzer was defined, either statically in the application context,
     * or created dynamically using the rest services.
     *
     * @param analysis the analysis to perform the analysis on, the input for the analyzer in other words
     * @return the analysis passed in, with the results of the analysis, and potentially the distribution,
     * and other statistical measures, including but not limited to the output of the underlying algorithm, which
     * might contain the confusion matrix among other things
     */
    Analysis<I, O> analyze(final Analysis<I, O> analysis);

    /**
     * This method will return the size of the class categories in the case of classifiers and the size of the
     * clusters in the case of clusters for the analyzers/models.
     *
     * @param analysis the analysis object to get the size of clusters and classes for
     * @return the analysis object, populated with the sizes of the clusters or classes for the analyzers
     */
    Analysis<I, O> sizesForClassesOrClusters(final Analysis<I, O> analysis);

    /**
     * This method returns all the contexts that are defined in the system./
     *
     * @return all the contexts that are defined, that have generated or been used to
     * initialize and build analyzers and models in the system
     */
    Map<String, Context> getContexts();

    /**
     * This method returns a specific context, based on the name given. All contexts and subsequently
     * analyzers have a unique name in the system.
     *
     * @param analyzerName the name of the analyzer to get the context for
     * @return the context associated with the name, or null if no context is associated with that name
     */
    Context getContext(final String analyzerName);

    /**
     * This method will destroy the analyzer, and release all resources.
     *
     * @param context the context/analyzer to destroy
     */
    void destroy(final Context context);

}