package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Context;

import java.io.Serializable;

/**
 * This class is just a serializable snippet of logic that can be distributed over the
 * wire and executed on a remote server, essentially creating the same analyzer in each
 * server in the cluster so that the analysis can be distributed throughout the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Creator extends Action<Boolean> implements Serializable {

    /**
     * The context object that will be used for creating the analyzer
     */
    private Context context;

    public Creator(final Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Boolean call() throws Exception {
        IAnalyticsService service = getAnalyticsService();
        IAnalyzer analyzer = context.getAnalyzer();
        analyzer.init(context);
        service.getContexts().put(context.getName(), context);
        return Boolean.FALSE;
    }
}
