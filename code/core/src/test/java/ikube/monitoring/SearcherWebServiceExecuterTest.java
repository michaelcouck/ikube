package ikube.monitoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
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
@Ignore
@Deprecated
public class SearcherWebServiceExecuterTest extends BaseTest {

	public SearcherWebServiceExecuterTest() {
		super(SearcherWebServiceExecuterTest.class);
	}

	@Test
	public void execute() throws Exception {
		ISearcherWebServiceExecuter searcherWebServiceExecuter = ApplicationContextManager.getBean(SearcherWebServiceExecuter.class);
		List<Map<String, String>> results = searcherWebServiceExecuter.execute();
		assertNotNull(results);
		assertTrue(results.size() > 0);
		ListenerManager.fireEvent(Event.SERVICE, System.currentTimeMillis(), null, Boolean.FALSE);
	}

}
