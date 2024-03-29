package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially distributing the training throughout
 * the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Train extends Action<Boolean> {

    /**
     * The analysis object to use for the training
     */
    private Analysis analysis;

    public Train(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Boolean call() throws Exception {
        // Get the analyzer on the local machine
        IAnalyticsService service = getAnalyticsService();
        Context context = service.getContext(analysis.getContext());
        if (context == null) {
            // Doesn't exist on this server
            return Boolean.FALSE;
        }
        IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        // And train it
        analyzer.train(context, analysis);
        return Boolean.TRUE;
    }
}
