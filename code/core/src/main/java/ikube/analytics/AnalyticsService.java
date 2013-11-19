package ikube.analytics;

import ikube.model.Analysis;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Timer;

import java.sql.Timestamp;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the service 'connects' to the correct implementation and executes
 * the analysis logic.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
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
