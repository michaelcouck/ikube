package ikube.analytics;

/**
 * This factory for analyzers is implemented as builder. Typically taking multiple simple objects and building a complex one, all the time shielding the complex
 * object from the complexity of it's construction.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class AnalyzerManager {

	@SuppressWarnings("unchecked")
	public static void buildAnalyzer(final IAnalyzer<?, ?> analyzer) throws Exception {
		analyzer.build(null);
		analyzer.train(null, null);
		analyzer.analyze(null);
	}

}
