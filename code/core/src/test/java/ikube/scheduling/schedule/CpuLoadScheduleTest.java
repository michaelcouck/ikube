package ikube.scheduling.schedule;

import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.scheduling.schedule.CpuLoadSchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
public class CpuLoadScheduleTest extends AbstractTest {

	private CpuLoadSchedule cpuLoadSchedule;

	public CpuLoadScheduleTest() {
		super(CpuLoadScheduleTest.class);
	}

	@Before
	public void before() throws Exception {
		cpuLoadSchedule = new CpuLoadSchedule();
		Mockit.setUpMock(ApplicationContextManagerMock.class);
		Deencapsulation.setField(cpuLoadSchedule, monitorService);
		Deencapsulation.setField(cpuLoadSchedule, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void handleNotification() {
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		when(indexContext.getName()).thenReturn("indexContext");
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		List<Snapshot> snapshots = addSnapshots(12, 4, 8.0, new ArrayList<Snapshot>());
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		when(server.isCpuThrottling()).thenReturn(Boolean.TRUE);

		cpuLoadSchedule.run();

		// The throttle must be more than 0
		Mockito.verify(indexContext).setThrottle(1);

		when(indexContext.getThrottle()).thenReturn(1l);
		cpuLoadSchedule.run();
		Mockito.verify(indexContext).setThrottle(2);

		snapshots = addSnapshots(12, 4, 0.5, new ArrayList<Snapshot>());
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		cpuLoadSchedule.run();
		when(indexContext.getThrottle()).thenReturn(2l);
		Mockito.verify(indexContext).setThrottle(1);
	}

	@Test
	public void handleNotificationNeverThrottled() {
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		snapshots = addSnapshots(6, 4, 0.5, snapshots);
		snapshots = addSnapshots(6, 4, 1.5, snapshots);
		when(indexContext.getSnapshots()).thenReturn(snapshots);

		cpuLoadSchedule.run();

		// Verify that the set throttle was never called
		Mockito.verify(indexContext, Mockito.never()).setThrottle(Mockito.anyLong());
	}

	private List<Snapshot> addSnapshots(final int number, final double processors, final double load, final List<Snapshot> snapshots) {
		for (int i = 0; i < number; i++) {
			Snapshot snapshot = Mockito.mock(Snapshot.class);
			when(snapshot.getAvailableProcessors()).thenReturn(processors);
			when(snapshot.getSystemLoad()).thenReturn(load);
			snapshots.add(snapshot);
		}
		return snapshots;
	}

}