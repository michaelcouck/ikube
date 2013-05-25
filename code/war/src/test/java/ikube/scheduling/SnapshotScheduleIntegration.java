package ikube.scheduling;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;
import ikube.Integration;
import ikube.model.IndexContext;
import ikube.scheduling.schedule.SnapshotSchedule;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Before;
import org.junit.Test;

public class SnapshotScheduleIntegration extends Integration {

	private SnapshotSchedule snapshotSchedule;

	@Before
	public void before() {
		snapshotSchedule = ApplicationContextManager.getBean(SnapshotSchedule.class);
	}

	@Test
	public void handleNotification() {
		double maxSnapshots = IConstants.MAX_SNAPSHOTS / 1000;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotSchedule.run();
		}
		for (IndexContext<?> indexContext : monitorService.getIndexContexts().values()) {
			logger.info("Snapshots : " + indexContext.getSnapshots().size());
			assertNotNull(indexContext.getSnapshot());
		}
	}

}