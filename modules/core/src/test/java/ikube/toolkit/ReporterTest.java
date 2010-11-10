package ikube.toolkit;

import org.junit.Test;

import ikube.BaseTest;
import ikube.model.Event;

public class ReporterTest extends BaseTest {

	@Test
	public void handleNotification() {
		ClusterManager.setWorking(indexContext, "actionName", Boolean.TRUE);
		Reporter reporter = new Reporter();
		Event event = new Event();
		event.setType(Event.REPORT);
		reporter.handleNotification(event);
		ClusterManager.setWorking(indexContext, "actionName", Boolean.FALSE);
	}

}
