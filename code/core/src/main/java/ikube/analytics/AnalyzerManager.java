package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;
import ikube.toolkit.ApplicationContextManager;

/**
 * This factory for analyzers is implemented as builder. Typically taking multiple simple objects and building a complex one, all the time shielding the complex
 * object from the complexity of it's construction.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class AnalyzerManager {

	public static final IAnalyzer<?, ?>[] buildAnalyzer(final Analysis<?, ?>... analyses) throws Exception {
		int index = 0;
		IAnalyzer<?, ?>[] analyzers = new IAnalyzer<?, ?>[analyses.length];
		for (final Analysis<?, ?> analysis : analyses) {
			Buildable buildable = analysis.getBuildable();
			String name = analysis.getAnalyzer();
			String type = buildable.getAnalyzerType();
			IAnalyzer<?, ?> analyzer = ApplicationContextManager.setBean(name, type);
			AnalyzerManager.buildAnalyzer(buildable, analyzer);
			analyzers[index] = analyzer;
		}
		return analyzers;
	}

	public static final IAnalyzer<?, ?>[] buildAnalyzer(final Buildable... buildables) throws Exception {
		int index = 0;
		IAnalyzer<?, ?>[] analyzers = new IAnalyzer<?, ?>[buildables.length];
		for (final Buildable buildable : buildables) {
			IAnalyzer<?, ?> analyzer = (IAnalyzer<?, ?>) Class.forName(buildable.getAlgorithmType()).newInstance();
			buildAnalyzer(buildable, analyzer);
			analyzers[index] = analyzer;
		}
		return analyzers;
	}

	public static final IAnalyzer<?, ?> buildAnalyzer(final Buildable buildable, final IAnalyzer<?, ?> analyzer) throws Exception {
		analyzer.init(buildable);
		analyzer.build(buildable);
		return analyzer;
	}

}