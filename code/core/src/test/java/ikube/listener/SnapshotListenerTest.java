package ikube.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Cascading;
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
public class SnapshotListenerTest extends ATest {

	@Cascading
	private IDataBase dataBase;
	@Cascading
	private IClusterManager clusterManager;
	private SnapshotListener snapshotListener;
	private File latestIndexDirectory;
	private IMonitorService monitorService;

	public SnapshotListenerTest() {
		super(SnapshotListenerTest.class);
	}

	@Before
	@SuppressWarnings("rawtypes")
	public void before() throws Exception {
		monitorService = Mockito.mock(IMonitorService.class);
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);
		
		latestIndexDirectory = createIndex(indexContext, "Any kind of data for the index");
		snapshotListener = new SnapshotListener();
		when(fsDirectory.fileLength(anyString())).thenReturn(Long.MAX_VALUE);
		when(fsDirectory.listAll()).thenReturn(new String[] { "file" });
		Mockit.setUpMocks();
		Mockit.setUpMock(ApplicationContextManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handleNotification() {
		Deencapsulation.setField(snapshotListener, monitorService);
		Deencapsulation.setField(snapshotListener, dataBase);
		Deencapsulation.setField(snapshotListener, clusterManager);
		Event event = new Event();
		event.setType(Event.PERFORMANCE);
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		long maxSnapshots = IConstants.MAX_SNAPSHOTS + 10;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotListener.handleNotification(event);
		}
		logger.info("Snapshots : " + snapshots.size());
		assertTrue(snapshots.size() < maxSnapshots);
	}

	@Test
	public void getDocsPerMinute() {
		Snapshot snapshot = new Snapshot();
		long docsPerMinute = snapshotListener.getDocsPerMinute(indexContext, snapshot);
		logger.info("Docs per minute : " + docsPerMinute);
		assertEquals(0, docsPerMinute);

		Snapshot previous = new Snapshot();
		previous.setTimestamp(System.currentTimeMillis() - 65000);
		previous.setNumDocs(125);

		snapshot.setNumDocs(250);
		snapshot.setTimestamp(System.currentTimeMillis());

		when(indexContext.getSnapshots()).thenReturn(Arrays.asList(previous));

		docsPerMinute = snapshotListener.getDocsPerMinute(indexContext, snapshot);
		logger.info("Docs per minute : " + docsPerMinute);
		assertTrue(docsPerMinute > 100 && docsPerMinute < 125);
	}
	
	@Test
	public void getSearchesPerMinute() {
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		Snapshot snapshot = new Snapshot();
		
		long searchesPerMinute = snapshotListener.getSearchesPerMinute(indexContext, snapshot);
		logger.info("Searches per minute : " + searchesPerMinute);
		assertEquals(0, searchesPerMinute);

		Snapshot previous = new Snapshot();
		previous.setTimestamp(System.currentTimeMillis() - 65000);
		previous.setTotalSearches(100);

		snapshot.setTotalSearches(200);
		snapshot.setTimestamp(System.currentTimeMillis());

		when(indexContext.getSnapshots()).thenReturn(Arrays.asList(previous));

		searchesPerMinute = snapshotListener.getSearchesPerMinute(indexContext, snapshot);
		logger.info("Searches per minute : " + searchesPerMinute);
		assertTrue(searchesPerMinute > 50 && searchesPerMinute < 100);
	}

	@Test
	public void getLatestIndexDirectory() throws Exception {
		Date latestIndexDirectoryDate = snapshotListener.getLatestIndexDirectoryDate(indexContext);
		logger.info("Latest index directory date : " + latestIndexDirectoryDate.getTime());
		assertTrue(latestIndexDirectoryDate.getTime() == Long.parseLong(latestIndexDirectory.getParentFile().getName()));
	}

	@Test
	public void getNumDocs() throws Exception {
		when(indexWriter.numDocs()).thenReturn(Integer.MAX_VALUE);
		long numDocs = snapshotListener.getNumDocs(indexContext);
		logger.info("Num docs : " + numDocs);
		assertEquals(Integer.MAX_VALUE, numDocs);

		when(indexContext.getIndexWriter()).thenReturn(null);
		when(indexReader.numDocs()).thenReturn(Integer.MIN_VALUE);
		numDocs = snapshotListener.getNumDocs(indexContext);
		logger.info("Num docs : " + numDocs);
		assertEquals(Integer.MIN_VALUE, numDocs);
	}

	@Test
	public void getIndexSize() throws Exception {
		createIndex(indexContext, "the ", "string ", "to add");
		long indexSize = snapshotListener.getIndexSize(indexContext);
		logger.info("Index size : " + indexSize);
		assertTrue("There must be some size in the index : ", indexSize > 0);
	}

}