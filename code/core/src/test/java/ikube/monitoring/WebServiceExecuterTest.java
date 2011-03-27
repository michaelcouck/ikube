package ikube.monitoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.monitoring.ISearcherWebServiceExecuter;
import ikube.monitoring.SearcherWebServiceExecuter;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class WebServiceExecuterTest extends BaseTest {

	public WebServiceExecuterTest() {
		super(WebServiceExecuterTest.class);
	}

	@Test
	@Ignore
	public void execute() throws Exception {
		ISearcherWebServiceExecuter searcherWebServiceExecuter = ApplicationContextManager.getBean(SearcherWebServiceExecuter.class);
		List<Map<String, String>> results = searcherWebServiceExecuter.execute();
		assertNotNull(results);
		assertTrue(results.size() > 0);
	}

}
