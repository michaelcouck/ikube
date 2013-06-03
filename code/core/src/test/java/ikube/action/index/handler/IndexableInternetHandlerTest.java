package ikube.action.index.handler;

import ikube.AbstractTest;
import ikube.model.IndexableInternet;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.concurrent.Future;

import org.junit.Test;
import org.mockito.Mockito;

public class IndexableInternetHandlerTest extends AbstractTest {

	@Test
	public void handleIndexable() throws Exception {
		IndexableInternet indexableInternet = new IndexableInternet();
		indexableInternet.setUrl("http://www.ikube.be/site");
		IndexableInternetHandler indexableInternetHandler = new IndexableInternetHandler();
		indexableInternet.setThreads(3);
		Mockito.when(indexContext.getThrottle()).thenReturn(1000l);
		List<Future<?>> futures = indexableInternetHandler.handleIndexable(indexContext, indexableInternet);
		// We don't really have to wait for this future anymore
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		logger.info("Done : " + futures);
	}

}
