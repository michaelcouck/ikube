package ikube.analytics;

import ikube.model.Analysis;

import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
public interface IAnalyticsService<I, O> {

    Map<String, IAnalyzer> getAnalyzers();

    Analysis<I, O> analyze(final Analysis<I, O> analysis);

}