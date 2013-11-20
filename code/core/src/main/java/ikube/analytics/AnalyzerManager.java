package ikube.analytics;

import ikube.model.Buildable;

/**
 * This factory for analyzers is implemented as builder. Typically taking multiple simple objects and building a complex one, all the time shielding the complex
 * object from the complexity of it's construction.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class AnalyzerManager {

	public static void buildAnalyzer(final Buildable buildable, final IAnalyzer<?, ?> analyzer) throws Exception {
		analyzer.init(buildable);
		analyzer.build(buildable);
	}

}
