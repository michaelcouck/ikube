package ikube.analytics;

import ikube.model.Analysis;
import ikube.toolkit.Timer;

import java.sql.Timestamp;
import java.util.Map;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the service 'connects' to the correct implementation and executes
 * the analysis logic.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class AnalyticsService implements IAnalyticsService {

	private Map<String, IAnalyzer<?, ?>> analyzers;

	@Override
	@SuppressWarnings("unchecked")
	public <I, O> Analysis<I, O> analyze(final Analysis<I, O> analysis) {
		final IAnalyzer<I, O> analyzer = (IAnalyzer<I, O>) analyzers.get(analysis.getAnalyzer());
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

	public void setAnalyzers(final Map<String, IAnalyzer<?, ?>> analyzers) {
		this.analyzers = analyzers;
	}

}
