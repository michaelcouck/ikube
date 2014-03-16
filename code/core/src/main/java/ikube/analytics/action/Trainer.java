package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Trainer extends Action<IAnalyzer> {

    private Analysis analysis;

    public Trainer(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer call() throws Exception {
        IAnalyzer analyzer = getAnalyticsService().getAnalyzer(analysis.getAnalyzer());
        System.out.println("Training remotely : " + analyzer);
        analyzer.train(analysis);
        return analyzer;
    }
}
