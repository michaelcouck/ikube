package ikube.analytics.action;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import org.apache.commons.lang.SerializationUtils;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Analyzer extends Action<Analysis> {

    private Analysis analysis;

    public Analyzer(final Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis call() throws Exception {
        IAnalyzer analyzer = getAnalyticsService().getAnalyzer(analysis.getAnalyzer());
        // System.out.println("Analyzing remotely : " + analyzer);
        analyzer.analyze(analysis);
        // long length = SerializationUtils.serialize(analysis).length;
        // System.out.println("Analysis return length : " + length);
        return analysis;
    }
}
