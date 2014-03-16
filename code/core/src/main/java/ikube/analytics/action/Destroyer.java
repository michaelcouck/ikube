package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Context;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Destroyer extends Action<Void> {

    private Context context;

    public Destroyer(final Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        Context context = (Context) getAnalyticsService().getContexts().remove(this.context.getName());
        System.out.println("Destroying remotely : " + context);
        if (context != null) {
            IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
            if (analyzer != null) {
                try {
                    analyzer.destroy(context);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}
