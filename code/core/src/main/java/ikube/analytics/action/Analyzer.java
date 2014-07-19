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
public class Analyzer extends Action<Analysis> implements Serializable {

    /**
     * The analysis object to do the analysis on :)
     */
    private Analysis analysis;

    public Analyzer(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        // Get the remote analytics service
        IAnalyticsService service = getAnalyticsService();
        Context context = service.getContext(analysis.getContext());
        IAnalyzer analyzer = context.getAnalyzer();
        analyzer.analyze(context, analysis);
        // And return the analysis to the caller, which may not be local
        return analysis;
    }
}
