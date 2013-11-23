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

	public static IAnalyzer<?, ?> buildAnalyzer(final Analysis<?, ?> analysis) throws Exception {
		Buildable buildable = analysis.getBuildable();
		String name = analysis.getAnalyzer();
		String type = buildable.getAnalyzerType();
		IAnalyzer<?, ?> analyzer = ApplicationContextManager.setBean(name, type);
		return AnalyzerManager.buildAnalyzer(buildable, analyzer);
	}

	public static IAnalyzer<?, ?> buildAnalyzer(final Buildable buildable) throws Exception {
		IAnalyzer<?, ?> analyzer = (IAnalyzer<?, ?>) Class.forName(buildable.getAlgorithmType()).newInstance();
		return buildAnalyzer(buildable, analyzer);
	}

	public static IAnalyzer<?, ?> buildAnalyzer(final Buildable buildable, final IAnalyzer<?, ?> analyzer) throws Exception {
		analyzer.init(buildable);
		analyzer.build(buildable);
		return analyzer;
	}

}
