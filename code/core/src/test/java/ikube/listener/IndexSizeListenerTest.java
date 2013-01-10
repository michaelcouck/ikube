package ikube.listener;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
		indexSizeListener = new IndexSizeListener();

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		ApplicationContextManagerMock.setIndexContext(indexContext);

		Snapshot snapshot = mock(Snapshot.class);
		when(snapshot.getIndexSize()).thenReturn(Long.MAX_VALUE);
		when(indexContext.getLastSnapshot()).thenReturn(snapshot);
		when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[] { indexWriter });
		File indexDirectory = FileUtilities.getFile(indexDirectoryPath + IConstants.SEP + "127.0.0.1.8000", Boolean.TRUE);
		when(fsDirectory.getDirectory()).thenReturn(indexDirectory);

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
		Event event = mock(Event.class);
		when(event.getType()).thenReturn(Event.TIMER);
		indexSizeListener.handleNotification(event);
		// We never call this because the mock doesn't really get the new index writer
		// so the logic never calls the close on the index writer
		verify(indexWriter, never()).close(Boolean.TRUE);
		IndexWriter[] indexWriters = indexContext.getIndexWriters();
		logger.info("Index writers : " + indexWriters.length);
		assertTrue(indexWriters.length == 1);

		indexSizeListener.handleNotification(event);
		indexWriters = indexContext.getIndexWriters();
		logger.info("Index writers : " + indexWriters.length);
		assertTrue(indexWriters.length == 1);
	}

}