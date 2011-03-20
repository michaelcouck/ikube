package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class CloseTest extends ATest {

	private transient final Close close = new Close();

	public CloseTest() {
		super(CloseTest.class);
	}

	@Before
	public void before() {
		when(INDEX.getMultiSearcher()).thenReturn(MULTI_SEARCHER);
	}

	@Test
	public void execute() throws IOException {
		String serverIndexDirectoryPath = getServerIndexDirectoryPath(INDEX_CONTEXT);
		File serverIndexDirectory = createIndex(new File(serverIndexDirectoryPath));
		boolean closed = close.execute(INDEX_CONTEXT);
		assertTrue("The index was open and it should have been closed in the action : ", closed);

		File anotherServerIndexDirectory = createIndex(new File(serverIndexDirectoryPath.replace(IP, "127.0.0.2")));
		// INDEX_CONTEXT.getIndex().setMultiSearcher(MULTI_SEARCHER);
		// when(INDEX_SEARCHER.getIndexReader()).thenReturn(INDEX_READER);
		// when(INDEX_READER.directory()).thenReturn(FS_DIRECTORY);
		when(LOCK.isLocked()).thenReturn(Boolean.FALSE);
		when(FS_DIRECTORY.getFile()).thenReturn(new File(serverIndexDirectoryPath));
		// when(FS_DIRECTORY.makeLock(anyString())).thenReturn(LOCK);
		// when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);

		closed = close.execute(INDEX_CONTEXT);
		assertTrue("The index was open and shouldhave been closed in the action : ", closed);

		INDEX_CONTEXT.getIndex().setMultiSearcher(MULTI_SEARCHER);
		when(LOCK.isLocked()).thenReturn(Boolean.TRUE);

		closed = close.execute(INDEX_CONTEXT);
		assertTrue("The index was open and should have been closed in the action : ", closed);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);
		assertFalse("The index should have been deleted : ", serverIndexDirectory.exists());
		assertFalse("The index should have been deleted : ", anotherServerIndexDirectory.exists());
	}

}