package ikube.analytics;

import ikube.BaseTest;
import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.AnalyzerInfo;
import ikube.model.Context;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.clusterers.SimpleKMeans;

import java.io.File;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-04-2014
 */
public class AnalyticsServiceIntegration extends BaseTest {

	private String line = "1,1,0,1,1,0,1,1";
	private String analyzerName = "bmw-browsers";
	private String analyzerModelFileName = "bmw-browsers.arff";

	private IAnalyticsService analyticsService;

	@Before
	public void before() {
		analyticsService = ApplicationContextManager.getBean(IAnalyticsService.class);
	}

	@After
	public void after() throws Exception {
		destroy();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void create() throws Exception {
		Context context = getContext(analyzerModelFileName, analyzerName);
		IAnalyzer analyzer = analyticsService.create(context);
		assertNotNull(analyzer);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void train() throws Exception {
		create();
		Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
		IAnalyzer analyzer = analyticsService.train(analysis);
		assertNotNull(analyzer);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void build() throws Exception {
		train();
		Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
		IAnalyzer analyzer = analyticsService.build(analysis);
		assertNotNull(analyzer);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void analyze() throws Exception {
		build();
		Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
		analysis = analyticsService.analyze(analysis);
		assertNotNull(analysis.getOutput());
	}

	@Test
	public void destroy() throws Exception {
		analyze();
		Context context = getContext(analyzerModelFileName, analyzerName);
		analyticsService.destroy(context);
		assertNull(analyticsService.getContext(analyzerName));
	}

	@SuppressWarnings("unchecked")
	private Context getContext(final String fileName, final String name) {
		File trainingDataFile = FileUtilities.findFileRecursively(new File("."), fileName);
		String trainingData = FileUtilities.getContent(trainingDataFile);

		Context context = new Context();
		context.setName(name);

		AnalyzerInfo analyzerInfo = new AnalyzerInfo();
		analyzerInfo.setAnalyzer(WekaClusterer.class.getName());
		analyzerInfo.setAlgorithm(SimpleKMeans.class.getName());

		context.setAnalyzerInfo(analyzerInfo);

		context.setOptions(new String[] { "-N", "6" });
		context.setMaxTraining(Integer.MAX_VALUE);
		context.setTrainingData(trainingData);

		return context;
	}

	private Analysis getAnalysis(final String analyzer, final String input) {
		Analysis<String, double[]> analysis = new Analysis<>();
		analysis.setAnalyzer(analyzer);
		analysis.setInput(input);
		analysis.setDistribution(Boolean.TRUE);
		analysis.setClassesAndClusters(Boolean.TRUE);
		analysis.setAlgorithm(Boolean.TRUE);
		analysis.setCorrelation(Boolean.TRUE);
		return analysis;
	}

}