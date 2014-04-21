package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Context;

import java.io.Serializable;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially destroying the analyzers on all machines
 * in the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Destroyer extends Action<Void> implements Serializable {

    /**
     * The context object that will be used for destroying the analyzer
     */
    private Context context;

    public Destroyer(final Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        // Get the local context, but infact we are on the remote machine of course
        Context context = (Context) getAnalyticsService().getContexts().remove(this.context.getName());
        if (context != null) {
            IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
            if (analyzer != null) {
                try {
                    // And destroy the analyzer
                    analyzer.destroy(context);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}
