package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.11.13
 * @version 01.00
 */
public class AnalyzerManagerTest extends AbstractTest {

	@Test
	public void buildAnalyzer() throws Exception {
		ApplicationContextManager.getApplicationContextFilesystem("src/test/resources/spring/spring-analytics.xml");
		IAnalyzer<String, String> analyzer = ApplicationContextManager.getBean("analyzer-em");
		String cluster = analyzer.analyze("35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES");
		// Verify that the manager had initialized the analyzer, that Spring configuration
		// is fine and that indeed the analyzer is correctly clustering at least one instance
		assertEquals("1", cluster);
	}

}
