package ikube.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
public class SnapshotListenerTest extends ATest {

	private File latestIndexDirectory;
	private SnapshotListener snapshotListener;

	public SnapshotListenerTest() {
		super(SnapshotListenerTest.class);
	}

	@Before
	public void before() throws Exception {
		snapshotListener = new SnapshotListener();

		latestIndexDirectory = createIndex(indexContext, "Any kind of data for the index");
		when(fsDirectory.fileLength(anyString())).thenReturn(Long.MAX_VALUE);
		when(fsDirectory.listAll()).thenReturn(new String[] { "file" });

		Mockit.setUpMock(ApplicationContextManagerMock.class);

		Deencapsulation.setField(snapshotListener, dataBase);
		Deencapsulation.setField(snapshotListener, monitorService);
		Deencapsulation.setField(snapshotListener, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void handleNotification() {
		indexContext = new IndexContext<Object>();
		indexContext.setIndexDirectoryPath("./indexes");
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		Mockito.doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Snapshot snapshot = (Snapshot) args[0];
				snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));
				// Object mock = invocation.getMock();
				// logger.info("Mock : " + mock + ", args : " + Arrays.deepToString(args));
				return null;
			}
		}).when(dataBase).persist(Mockito.any(Snapshot.class));

		Event event = new Event();
		event.setType(Event.PERFORMANCE);
		long maxSnapshots = IConstants.MAX_SNAPSHOTS + 10;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotListener.handleNotification(event);
		}
		for (IndexContext<?> indexContext : monitorService.getIndexContexts().values()) {
			logger.info("Snapshots : " + indexContext.getSnapshots().size());
			assertTrue("There must be less snapshots than the maximum allowed : ", indexContext.getSnapshots().size() < maxSnapshots);
		}
	}

	@Test
	public void getDocsPerMinute() {
		Snapshot snapshot = new Snapshot();
		long docsPerMinute = snapshotListener.getDocsPerMinute(indexContext, snapshot);
		logger.info("Docs per minute : " + docsPerMinute);
		assertEquals(0, docsPerMinute);

		Snapshot previous = new Snapshot();
		previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
		previous.setNumDocs(125);

		snapshot.setNumDocs(250);
		snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

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
		previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
		previous.setTotalSearches(100);

		snapshot.setTotalSearches(200);
		snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

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
		assertEquals(2147483648l, numDocs);

		when(indexContext.getIndexWriter()).thenReturn(null);
		when(indexReader.numDocs()).thenReturn(Integer.MIN_VALUE);
		numDocs = snapshotListener.getNumDocs(indexContext);
		logger.info("Num docs : " + numDocs);
		assertEquals(-2147483648l, numDocs);
	}

	@Test
	public void getIndexSize() throws Exception {
		createIndex(indexContext, "the ", "string ", "to add");
		long indexSize = snapshotListener.getIndexSize(indexContext);
		logger.info("Index size : " + indexSize);
		assertTrue("There must be some size in the index : ", indexSize > 0);
	}

}