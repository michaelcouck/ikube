package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class CloseTest extends BaseActionTest {

	private Close close = new Close();

	@Test
	public void execute() throws Exception {
		indexContext.setMultiSearcher(MULTI_SEARCHER);

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File serverIndexDirectory = createIndex(new File(serverIndexDirectoryPath));
		boolean closed = close.execute(indexContext);
		assertTrue(closed);

		File anotherServerIndexDirectory = createIndex(new File(serverIndexDirectoryPath.replace(IP, "127.0.0.2")));
		indexContext.setMultiSearcher(MULTI_SEARCHER);
		// when(INDEX_SEARCHER.getIndexReader()).thenReturn(INDEX_READER);
		// when(INDEX_READER.directory()).thenReturn(FS_DIRECTORY);
		when(LOCK.isLocked()).thenReturn(Boolean.FALSE);
		when(FS_DIRECTORY.getFile()).thenReturn(new File(serverIndexDirectoryPath));
		// when(FS_DIRECTORY.makeLock(anyString())).thenReturn(LOCK);
		// when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);

		closed = close.execute(indexContext);
		assertTrue(closed);

		indexContext.setMultiSearcher(MULTI_SEARCHER);
		when(LOCK.isLocked()).thenReturn(Boolean.TRUE);

		closed = close.execute(indexContext);
		assertTrue(closed);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);
		assertFalse(serverIndexDirectory.exists());
		assertFalse(anotherServerIndexDirectory.exists());
	}

}