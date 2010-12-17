package ikube;

import ikube.model.Event;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Ignore;
import org.junit.Test;

public class IndexEngineTest extends BaseTest {

	@Test
	@Ignore
	public void handleNotification() {
		IndexEngine indexEngine = ApplicationContextManager.getBean(IndexEngine.class);
		Event event = new Event();
		event.setTimestamp(System.currentTimeMillis());
		event.setType(Event.TIMER);
		indexEngine.handleNotification(event);

		// In the configuration there are three contexts, each one using the
		// same indexables. There is a top level table(tableOne) that has a three
		// level structure for indexing, this table is used three times, so in total
		// there should be 3 * 3 iterations over the table data. We need to verify
		// that this has in fact happened
	}

	public static void main(String[] args) {
		new IndexEngineTest().handleNotification();
	}

}
