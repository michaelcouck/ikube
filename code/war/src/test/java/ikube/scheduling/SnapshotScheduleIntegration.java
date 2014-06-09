package ikube.scheduling;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.model.IndexContext;
import ikube.scheduling.schedule.SnapshotSchedule;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SnapshotScheduleIntegration extends IntegrationTest {

    @Autowired
	private SnapshotSchedule snapshotSchedule;

	@Test
	public void handleNotification() {
		double maxSnapshots = IConstants.MAX_SNAPSHOTS / 1000;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotSchedule.run();
		}
		for (IndexContext indexContext : monitorService.getIndexContexts().values()) {
			logger.info("Snapshots : " + indexContext.getSnapshots().size());
			assertNotNull(indexContext.getSnapshot());
		}
	}

}