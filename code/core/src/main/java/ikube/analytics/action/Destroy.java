package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Context;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially destroying the analyzers on all machines
 * in the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Destroy extends Action<Boolean> {

    /**
     * The context object that will be used for destroying the analyzer
     */
    private Context context;

    public Destroy(final Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Boolean call() throws Exception {
        IAnalyticsService service = getAnalyticsService();
        // Get the local context
        context = service.getContext(context.getName());
        if (context == null) {
            // Doesn't exist on this server
            return Boolean.FALSE;
        }
        // Get the local context, but in fact we are on the remote machine of course
        service.getContexts().remove(context.getName());
        IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        // And destroy the analyzer
        analyzer.destroy(context);
        return Boolean.TRUE;
    }
}