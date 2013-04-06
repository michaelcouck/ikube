package ikube.scheduling.schedule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.scheduling.schedule.SnapshotSchedule;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
public class SnapshotScheduleTest extends AbstractTest {

	private SnapshotSchedule snapshotSchedule;

	public SnapshotScheduleTest() {
		super(SnapshotScheduleTest.class);
	}

	@Before
	public void before() throws Exception {
		snapshotSchedule = new SnapshotSchedule();

		when(fsDirectory.fileLength(anyString())).thenReturn(Long.MAX_VALUE);
		when(fsDirectory.listAll()).thenReturn(new String[] { "file" });

		Mockit.setUpMock(ApplicationContextManagerMock.class);

		Deencapsulation.setField(snapshotSchedule, dataBase);
		Deencapsulation.setField(snapshotSchedule, monitorService);
		Deencapsulation.setField(snapshotSchedule, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void handleNotification() {
		IndexContext indexContext = new IndexContext<Object>();
		indexContext.setIndexDirectoryPath("./indexes");
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		Mockito.doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Snapshot snapshot = (Snapshot) args[0];
				snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));
				return null;
			}
		}).when(dataBase).persist(Mockito.any(Snapshot.class));

		double maxSnapshots = IConstants.MAX_SNAPSHOTS + 10d;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotSchedule.run();
		}
		logger.info("Snapshots : " + indexContext.getSnapshots().size());
		assertTrue("There must be less snapshots than the maximum allowed : ", indexContext.getSnapshots().size() < maxSnapshots);
	}

	@Test
	public void getDocsPerMinute() {
		Snapshot snapshot = new Snapshot();
		long docsPerMinute = snapshotSchedule.getDocsPerMinute(indexContext, snapshot);
		logger.info("Docs per minute : " + docsPerMinute);
		assertEquals(0, docsPerMinute);

		Snapshot previous = new Snapshot();
		previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
		previous.setNumDocs(125);

		snapshot.setNumDocs(250);
		snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

		when(indexContext.getSnapshots()).thenReturn(Arrays.asList(previous));

		docsPerMinute = snapshotSchedule.getDocsPerMinute(indexContext, snapshot);
		logger.info("Docs per minute : " + docsPerMinute);
		assertTrue(docsPerMinute > 100 && docsPerMinute < 125);
	}

	@Test
	public void getSearchesPerMinute() {
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		Snapshot snapshot = new Snapshot();

		long searchesPerMinute = snapshotSchedule.getSearchesPerMinute(indexContext, snapshot);
		logger.info("Searches per minute : " + searchesPerMinute);
		assertEquals(0, searchesPerMinute);

		Snapshot previous = new Snapshot();
		previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
		previous.setTotalSearches(100);

		snapshot.setTotalSearches(200);
		snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

		when(indexContext.getSnapshots()).thenReturn(Arrays.asList(previous));

		searchesPerMinute = snapshotSchedule.getSearchesPerMinute(indexContext, snapshot);
		logger.info("Searches per minute : " + searchesPerMinute);
		assertTrue(searchesPerMinute > 50 && searchesPerMinute < 100);
	}

}