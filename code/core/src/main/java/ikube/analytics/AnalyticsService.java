package ikube.analytics;

import ikube.model.Analysis;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Timer;

import java.sql.Timestamp;
import java.util.Map;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the service 'connects' to the correct implementation and executes
 * the analysis logic.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public class AnalyticsService<I, O> implements IAnalyticsService<I, O> {

    private Map<String, IAnalyzer> analyzers;

    @Override
    public Map<String, IAnalyzer> getAnalyzers() {
        return analyzers;
    }

    @Override
    public Analysis<I, O> analyze(final Analysis<I, O> analysis) {
        final IAnalyzer<I, O> analyzer = getAnalyzer(analysis);
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            @SuppressWarnings("unchecked")
            public void execute() {
                try {
                    O output = analyzer.analyze((I) analysis);
                    analysis.setOutput(output);
                } catch (final Exception e) {
                    analysis.setException(e);
                }
            }
        });
        analysis.setDuration(duration);
        analysis.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return analysis;
    }

    @SuppressWarnings("unchecked")
    IAnalyzer<I, O> getAnalyzer(final Analysis<I, O> analysis) {
        Map<String, IAnalyzer> analyzers = getAnalyzers();
        IAnalyzer<?, ?> analyzer = analyzers.get(analysis.getAnalyzer());
        return (IAnalyzer<I, O>) analyzer;
    }

    public void setAnalyzers(final Map<String, IAnalyzer> analyzers) {
        this.analyzers = analyzers;
    }

}