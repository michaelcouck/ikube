package ikube.scheduling;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.cluster.IMonitorService;
import ikube.model.IndexContext;
import ikube.scheduling.schedule.SnapshotSchedule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SnapshotScheduleIntegration extends IntegrationTest {

    @Autowired
    private SnapshotSchedule snapshotSchedule;
    @Autowired
    protected IMonitorService monitorService;

    @Test
    public void handleNotification() {
        double maxSnapshots = IConstants.MAX_SNAPSHOTS / 1000;
        for (int i = 0; i < maxSnapshots; i++) {
            snapshotSchedule.run();
        }
        for (final IndexContext indexContext : monitorService.getIndexContexts().values()) {
            assertNotNull(indexContext.getSnapshot());
        }
    }

}