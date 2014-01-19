package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.11.13
 * @version 01.00
 */
@Ignore
public class AnalyzerManagerTest extends AbstractTest {

	@Test
	public void buildAnalyzer() throws Exception {
		try {
			ApplicationContextManager.closeApplicationContext();
			ApplicationContextManager.getApplicationContextFilesystem("src/test/resources/spring/spring-analytics.xml");
			IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> analyzer = ApplicationContextManager.getBean("analyzer-em");
			Analysis<String, double[]> analysis = getAnalysis(null, "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES");
			Analysis<String, double[]> cluster = analyzer.analyze(analysis);
			// Verify that the manager had initialized the analyzer, that Spring configuration
			// is fine and that indeed the analyzer is correctly clustering at least one instance
			assertEquals(1, cluster.getClazz());
		} finally {
			ApplicationContextManager.closeApplicationContext();
		}
	}

}
