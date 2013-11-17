package ikube.analytics;

import ikube.model.Analysis;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Timer;

import java.sql.Timestamp;

public class AnalyticsService implements IAnalyticsService {

	@Override
	public <I, O> Analysis<I, O> analyze(final Analysis<I, O> analysis) {
		// Get the analyzer defined from the context, this can be replaced with an auto wired map perhaps?
		final IAnalyzer<I, O> analyzer = ApplicationContextManager.getBean(analysis.getAnalyzer());
		double duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {
				try {
					O output = analyzer.analyze(analysis.getInput());
					analysis.setOutput(output);
				} catch (Exception e) {
					analysis.setException(e);
				}
			}
		});
		analysis.setDuration(duration);
		analysis.setTimestamp(new Timestamp(System.currentTimeMillis()));
		return analysis;
	}

}
