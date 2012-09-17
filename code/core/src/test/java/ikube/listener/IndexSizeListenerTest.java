package ikube.listener;

import ikube.ATest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Snapshot;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
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

	public IndexSizeListenerTest() {
		super(IndexSizeListenerTest.class);
	}

	@Before
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
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
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

}
