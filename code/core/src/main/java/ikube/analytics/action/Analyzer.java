package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;

public class Analyzer extends Action<Analysis> {

    private Analysis analysis;

    public Analyzer(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        IAnalyzer analyzer = getAnalyticsService().getAnalyzer(analysis.getAnalyzer());
        analyzer.analyze(analysis);
        return analysis;
    }
}
