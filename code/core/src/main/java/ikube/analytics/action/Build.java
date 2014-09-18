package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially distributing the building of all the
 * analyzers throughout the cluster so that they are all equally trained.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Build extends Action<Boolean> {

    /**
     * The analysis object to do the building with
     */
    private Analysis analysis;

    public Build(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Boolean call() throws Exception {
        // Get the remote analyzer service from Spring
        IAnalyticsService service = getAnalyticsService();
        Context context = service.getContext(analysis.getContext());
        if (context != null) {
            IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
            // And build the remote analyzer
            analyzer.build(context);
            return Boolean.TRUE;
        }
        // Doesn't exist on this server
        return Boolean.FALSE;
    }

}
