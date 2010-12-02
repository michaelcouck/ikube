package ikube;

import ikube.model.Event;
import ikube.toolkit.ApplicationContextManager;

import java.sql.Timestamp;

import org.junit.Test;

public class IndexEngineTest extends BaseTest {

	@Test
	public void handleNotification() {
		IndexEngine indexEngine = ApplicationContextManager.getBean(IndexEngine.class);
		Event event = new Event();
		event.setIndexContext(indexContext);
		event.setTimestamp(new Timestamp(System.currentTimeMillis()));
		event.setType(Event.TIMER);
		indexEngine.handleNotification(event);
		// This test must just not throw exceptions, the
		// sub components are tested seperately
	}

}
