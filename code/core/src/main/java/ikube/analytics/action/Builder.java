package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Builder extends Action<Void> {

    private Analysis analysis;

    public Builder(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        Context context = getAnalyticsService().getContext(analysis.getAnalyzer());
        IAnalyzer analyzer = (IAnalyzer) context.getAnalyzer();
        System.out.println("Building remotely : " + context + ", " + analyzer);
        analyzer.build(context);
        return null;
    }

}
