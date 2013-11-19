package ikube.analytics;

import ikube.model.Analysis;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public interface IAnalyticsService {

	<I, O> Analysis<I, O> analyze(Analysis<I, O> analysis);

}