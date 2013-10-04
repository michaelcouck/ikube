package ikube.analytics;

import ikube.model.Analysis;

public interface IAnalyticsService {

	<I, O> Analysis<I, O> analyze(Analysis<I, O> analysis);

}