package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Context;

/**
 * This class will create an analyzer, potentially on a remove server, using the {@link ikube.analytics.IAnalyzer}
 * on the remote machine, and indeed the {@link ikube.model.Context} on the remote machine.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Creator extends Action<Boolean> {

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
        if (String.class.isAssignableFrom(context.getAnalyzer().getClass())) {
            context.setAnalyzer(Class.forName(context.getAnalyzer().toString()).newInstance());
        }
        IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        if (analyzer != null) {
            analyzer.init(context);
            service.getContexts().put(context.getName(), context);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
