package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;

import java.io.Serializable;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially distributing the analysis throughout
 * the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class SizesForClassesOrClusters extends Action<Analysis> implements Serializable {

    /**
     * The analysis object to use for the analysis
     */
    private Analysis analysis;

    public SizesForClassesOrClusters() {
        this(null);
    }

    public SizesForClassesOrClusters(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        IAnalyticsService analyticsService = getAnalyticsService();
        Context context = analyticsService.getContext(analysis.getContext());
        IAnalyzer analyzer = context.getAnalyzer();
        analyzer.analyze(context, analysis);
        return analysis;
    }
}
