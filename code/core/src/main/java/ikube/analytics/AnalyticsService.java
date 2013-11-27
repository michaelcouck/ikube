package ikube.analytics;

import ikube.model.Analysis;
import ikube.toolkit.Timer;

import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the service 'connects' to the correct implementation and executes
 * the analysis logic.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class AnalyticsService implements IAnalyticsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

	private Map<String, IAnalyzer<?, ?>> analyzers;

	@Override
	public <I, O> Analysis<I, O> analyze(final Analysis<I, O> analysis) {
		final IAnalyzer<I, O> analyzer = getAnalyzer(analysis);
		double duration = Timer.execute(new Timer.Timed() {
			@Override
			@SuppressWarnings("unchecked")
			public void execute() {
				try {
					analyzer.analyze((I) analysis);
				} catch (Exception e) {
					analysis.setException(e);
				}
			}
		});
		analysis.setDuration(duration);
		analysis.setTimestamp(new Timestamp(System.currentTimeMillis()));
		return analysis;
	}

	@SuppressWarnings("unchecked")
	<I, O> IAnalyzer<I, O> getAnalyzer(final Analysis<I, O> analysis) {
		IAnalyzer<?, ?> analyzer = analyzers.get(analysis.getAnalyzer());
		if (analyzer == null) {
			try {
				analyzer = AnalyzerManager.buildAnalyzer(analysis)[0];
				analyzers.put(analysis.getAnalyzer(), analyzer);
			} catch (Exception e) {
				LOGGER.error(null, e);
				throw new RuntimeException(e);
			}
		}
		return (IAnalyzer<I, O>) analyzer;
	}

	public Map<String, IAnalyzer<?, ?>> getAnalyzers() {
		return analyzers;
	}

	public void setAnalyzers(final Map<String, IAnalyzer<?, ?>> analyzers) {
		this.analyzers = analyzers;
	}

}