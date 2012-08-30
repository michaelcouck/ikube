package ikube.listener;

import ikube.ATest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mockit.Deencapsulation;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 29.08.12
 * @version 01.00
 */
public class IndexSizeListenerTest extends ATest {

	private IMonitorService monitorService;
	/** Class under test. */
	private IndexSizeListener indexSizeListener;

	public IndexSizeListenerTest() {
		super(IndexSizeListenerTest.class);
	}

	@Before
	@SuppressWarnings("rawtypes")
	public void before() {
		monitorService = Mockito.mock(IMonitorService.class);
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		Mockito.when(monitorService.getIndexContexts()).thenReturn(indexContexts);
		Snapshot snapshot = Mockito.mock(Snapshot.class);
		Mockito.when(snapshot.getIndexSize()).thenReturn(Long.MAX_VALUE);
		Mockito.when(indexContext.getLastSnapshot()).thenReturn(snapshot);

		File indexDirectory = FileUtilities.getFile(indexDirectoryPath + IConstants.SEP + "127.0.0.1.8000", Boolean.TRUE);
		Mockito.when(fsDirectory.getFile()).thenReturn(indexDirectory);

		indexSizeListener = new IndexSizeListener();
		Deencapsulation.setField(indexSizeListener, monitorService);
	}

	@Test
	public void handleNotification() throws CorruptIndexException, IOException {
		// Event event
		Event event = Mockito.mock(Event.class);
		Mockito.when(event.getType()).thenReturn(Event.TIMER);
		indexSizeListener.handleNotification(event);
		// We never call this because the mock doesn't really get the new index writer
		// so the logic never calls the close on the index writer
		Mockito.verify(indexWriter, Mockito.never()).close(Boolean.TRUE);
		Mockito.verify(indexContext, Mockito.atLeastOnce()).setIndexWriter(Mockito.any(IndexWriter.class));
	}

}
