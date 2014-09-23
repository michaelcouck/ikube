package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Context;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @see ikube.analytics.IAnalyzer for details on the parameterized types
 * @since 10-04-2013
 */
public interface IAnalyticsService<I, O> {

    Context create(final Context context);

    boolean upload(final String filePath, final InputStream inputStream);

    Object[][][] data(final Context context, final int rows);

    Context train(final Analysis<I, O> analysis);

    Context build(final Analysis<I, O> analysis);

    Analysis<I, O> analyze(final Analysis<I, O> analysis);

    Analysis<I, O> sizesForClassesOrClusters(final Analysis<I, O> analysis);

    Map<String, Context> getContexts();

    Context getContext(final String analyzerName);

    void destroy(final Context context);

}