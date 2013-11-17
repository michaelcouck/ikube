package ikube.analytics;

public class AnalyzerManager {

	@SuppressWarnings("unchecked")
	public static void buildAnalyzer() throws Exception {
		IAnalyzer<?, ?> analyzer = new WekaAnalyzer();
		analyzer.initialize();
		analyzer.train(null, null);
		analyzer.build();
		analyzer.analyze(null);
	}

}
