package ikube.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 29.08.12
 * @version 01.00
 */
public class IndexSizeListenerTest extends ATest {

	/** Class under test. */
	private IndexSizeListener indexSizeListener;
	private IMonitorService monitorService;

	public IndexSizeListenerTest() {
		super(IndexSizeListenerTest.class);
	}

	@Before
	@SuppressWarnings("rawtypes")
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		ApplicationContextManagerMock.setIndexContext(indexContext);
		Snapshot snapshot = Mockito.mock(Snapshot.class);
		// Mockito.when(clusterManager.getServer()).thenReturn(server);
		Mockito.when(snapshot.getIndexSize()).thenReturn(Long.MAX_VALUE);
		Mockito.when(indexContext.getLastSnapshot()).thenReturn(snapshot);
		Mockito.when(indexContext.getIndexWriter()).thenReturn(indexWriter);
		File indexDirectory = FileUtilities.getFile(indexDirectoryPath + IConstants.SEP + "127.0.0.1.8000", Boolean.TRUE);
		Mockito.when(fsDirectory.getFile()).thenReturn(indexDirectory);
		indexSizeListener = new IndexSizeListener();
		Deencapsulation.setField(indexSizeListener, clusterManager);

		monitorService = mock(IMonitorService.class);
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		Deencapsulation.setField(indexSizeListener, monitorService);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handleNotification() throws CorruptIndexException, IOException {
		Event event = Mockito.mock(Event.class);
		Mockito.when(event.getType()).thenReturn(Event.TIMER);
		indexSizeListener.handleNotification(event);
		// We never call this because the mock doesn't really get the new index writer
		// so the logic never calls the close on the index writer
		Mockito.verify(indexWriter, Mockito.never()).close(Boolean.TRUE);
		Mockito.verify(indexContext, Mockito.atLeastOnce()).setIndexWriter(Mockito.any(IndexWriter.class));
	}

	@Test
	public void getIndexSize() throws Exception {
		File serverIndexDirectory = createIndex(indexContext, "The index data");
		logger.info("Server index directory : " + serverIndexDirectory);

		long indexSize = indexSizeListener.getIndexSize(indexContext);
		assertEquals("There should be no index size found : ", 0, indexSize);

		File latestServerIndexDirectory = createIndex(indexContext, "The second index data", "Which has more data in it");
		Directory directory = FSDirectory.open(latestServerIndexDirectory);
		Lock lock = getLock(directory, latestServerIndexDirectory);

		indexSize = indexSizeListener.getIndexSize(indexContext);
		assertTrue("The locked directory is the index that is open : ", indexSize > 0);

		lock.release();
	}

}