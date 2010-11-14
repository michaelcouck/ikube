package ikube.toolkit;

import ikube.BaseTest;
import ikube.cluster.IClusterManager;
import ikube.model.Event;

import org.junit.Test;

public class ReporterTest extends BaseTest {

	@Test
	public void handleNotification() {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.setWorking(indexContext, "actionName", Boolean.TRUE, System.currentTimeMillis());
		Reporter reporter = new Reporter();
		Event event = new Event();
		event.setType(Event.REPORT);
		reporter.handleNotification(event);
		clusterManager.setWorking(indexContext, "actionName", Boolean.FALSE, 0);
	}

}
