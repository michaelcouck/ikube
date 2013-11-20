package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Buildable;

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
	private String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";

	@Before
	public void before() throws Exception {
		buildable = new Buildable();
		buildable.setFilePath("bank-data.arff");
		buildable.setType(EM.class.getName());
		analyzer = new WekaClusterer();
		analyzer.init(buildable);
		analyzer.build(buildable);
		analyticsService = new AnalyticsService();
		analyticsService.setAnalyzers(new HashMap<String, IAnalyzer<?, ?>>() {
			{
				put("analyzer-em", analyzer);
			}
		});
	}

	@Test
	public void analyze() {
		Analysis<String, String> analysis = new Analysis<String, String>();
		analysis.setAnalyzer("analyzer-em");
		analysis.setInput(line);
		analyticsService.analyze(analysis);
		assertEquals("1", analysis.getOutput());
	}

}
