package ikube.web.service;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.AnalyzerInfo;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HttpClientUtilities;
import org.junit.Test;
import weka.clusterers.SimpleKMeans;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * TODO: Note to self. For some reason this test does not work on the Dell server!!!
 * TODO: Does now it seems... :)
 * TODO: This test must still be completed and verified, perhaps with all sorts of analytics, like regression etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-02-2014
 */
public class AnalyzerIntegration extends BaseTest {

	private String line = "1,1,0,1,1,0,1,1";
	private String analyzerName = "bmw-browsers";
	private String analyzerModelFileName = "bmw-browsers.arff";

	@Test
	@SuppressWarnings("unchecked")
	public void create() throws Exception {
		Context context = getContext(analyzerModelFileName, analyzerName);
		String url = getUrl(Analyzer.CREATE);
		HttpClientUtilities.doPost(url, context, Context.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void train() throws Exception {
		create();

		Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
		String url = getUrl(Analyzer.TRAIN);
		HttpClientUtilities.doPost(url, analysis, Analysis.class);
	}

	@Test
	public void build() throws Exception {
		train();

		Analysis analysis = getAnalysis(analyzerName, null);
		String url = getUrl(Analyzer.BUILD);
		HttpClientUtilities.doPost(url, analysis, Analysis.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void analyze() throws Exception {
		build();

		Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
		String url = getUrl(Analyzer.ANALYZE);
		Analysis result = HttpClientUtilities.doPost(url, analysis, Analysis.class);
        // TODO: Fix this test!!!!!!!!
		// assertTrue(Integer.parseInt(result.getClazz()) >= 0 && Integer.parseInt(result.getClazz()) <= 6);
	}

	@Test
	public void destroy() throws Exception {
		analyze();

		String analyzersUrl = getUrl(Analyzer.ANALYZERS);
		String[] names = HttpClientUtilities.doGet(analyzersUrl, String[].class);
		List<String> list = new ArrayList<>(Arrays.asList(names));
		assertTrue(list.contains(analyzerName));

		Context context = getContext(analyzerModelFileName, analyzerName);
		String destroyUrl = getUrl(Analyzer.DESTROY);

		HttpClientUtilities.doPost(destroyUrl, context, Context.class);

		names = HttpClientUtilities.doGet(analyzersUrl, String[].class);
		list = new ArrayList<>(Arrays.asList(names));
		assertFalse(list.contains(analyzerName));
	}

	@Test
	public void context() throws Exception {
		analyze();

		Analysis analysis = getAnalysis(analyzerName, null);
		String url = getUrl(Analyzer.CONTEXT);
		Context context = HttpClientUtilities.doPost(url, analysis, Context.class);
        // TODO: Fix this test!!!!!!!!
		// assertEquals(analysis.getAnalyzer(), context.getName());
	}

	@Test
	public void contexts() throws Exception {
		analyze();

		String contextsUrl = getUrl(Analyzer.CONTEXTS);
		String[] names = HttpClientUtilities.doGet(contextsUrl, String[].class);
		List<String> list = new ArrayList<>(Arrays.asList(names));
		assertTrue(list.contains(analyzerName));
	}

	@Test
	public void analyzers() throws Exception {
		create();

		String analyzersUrl = getUrl(Analyzer.ANALYZERS);
		String[] names = HttpClientUtilities.doGet(analyzersUrl, String[].class);
		List<String> list = new ArrayList<>(Arrays.asList(names));
		logger.info("List : " + list);
		assertTrue(list.toString().contains(analyzerName));
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	protected String getUrl(final String service) throws MalformedURLException {
		StringBuilder builder = new StringBuilder();
		builder.append(IConstants.SEP);
		builder.append(IConstants.IKUBE);
		builder.append(BaseTest.SERVICE);
		builder.append(Analyzer.ANALYZER);
		builder.append(service);
		return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
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