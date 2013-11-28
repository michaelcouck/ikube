package ikube.analytics;

import ikube.model.Analysis;

import java.util.Map;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public interface IAnalyticsService {

	<I, O> Analysis<I, O> analyze(final Analysis<I, O> analysis);

	Map<String, IAnalyzer<?, ?>> getAnalyzers();

	void setAnalyzers(final Map<String, IAnalyzer<?, ?>> analyzers);

}