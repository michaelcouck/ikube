package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;

/**
 * This class will perform an analysis on a specific {@link ikube.model.Context}, and with a
 * specific {@link ikube.model.Analysis} object as input. It is a serializable to be executed on
 * a remote server.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Analyze extends Action<Analysis> {

    /**
     * The analysis object to do the analysis on :)
     */
    private Analysis analysis;

    public Analyze(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        try {
            // Get the remote analytics service
            IAnalyticsService service = getAnalyticsService();
            Context context = service.getContext(analysis.getContext());
            if (context != null) {
                IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
                analyzer.analyze(context, analysis);
            }
            // And return the analysis to the caller, which may not be local
            return analysis;
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
