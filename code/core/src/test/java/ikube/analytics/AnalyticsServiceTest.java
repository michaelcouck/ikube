package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Buildable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import weka.clusterers.EM;

/**
 * @author Michael Couck
 * @since 20.11.13
 * @version 01.00
 */
public class AnalyticsServiceTest extends AbstractTest {

	private Buildable buildable;
	private IAnalyzer<?, ?> analyzer;
	private AnalyticsService analyticsService;
	private Analysis<String, double[]> analysis;
	private String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";

	@Before
	public void before() throws Exception {
		analysis = new Analysis<String, double[]>();
		analysis.setAnalyzer("analyzer-em");
		analysis.setInput(line);

		buildable = new Buildable();
		buildable.setTrainingFilePath("bank-data.arff");
		buildable.setAlgorithmType(EM.class.getName());
		analyzer = new WekaClusterer();
		analyzer.init(buildable);
		analyzer.build(buildable);
		analyticsService = new AnalyticsService();
		analyticsService.setAnalyzers(new HashMap<String, IAnalyzer<?, ?>>() {
			{
				put(analysis.getAnalyzer(), analyzer);
			}
		});
	}

	@Test
	public void analyze() {
		analyticsService.analyze(analysis);
		assertEquals(1, analysis.getClazz());
	}

	@Test
	public void getAnalyzer() {
		File externalConfig = FileUtilities.findDirectoryRecursively(new File("."), "external");
		File springConfig = FileUtilities.findFileRecursively(externalConfig, "spring\\.xml");
		String springConfigPath = FileUtilities.cleanFilePath(springConfig.getAbsolutePath());
		System.setProperty(IConstants.IKUBE_CONFIGURATION, springConfigPath);

		analysis.setAnalyzer("analyzer-em-different");
		analysis.setBuildable(buildable);

		buildable.setAlgorithmType(EM.class.getName());
		buildable.setAnalyzerType(WekaClusterer.class.getName());

		IAnalyzer<?, ?> analyzer = analyticsService.getAnalyzer(analysis);
		assertNotNull(analyzer);
	}

}