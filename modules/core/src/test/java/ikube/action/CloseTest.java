package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
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
		indexContext.setMultiSearcher(multiSearcher);

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File serverIndexDirectory = createIndex(new File(serverIndexDirectoryPath));
		boolean closed = close.execute(indexContext);
		assertTrue(closed);

		File anotherServerIndexDirectory = createIndex(new File(serverIndexDirectoryPath.replace(ip, "127.0.0.2")));
		indexContext.setMultiSearcher(multiSearcher);
		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.directory()).thenReturn(fsDirectory);
		when(lock.isLocked()).thenReturn(Boolean.FALSE);
		when(fsDirectory.getFile()).thenReturn(new File(serverIndexDirectoryPath));
		when(fsDirectory.makeLock(anyString())).thenReturn(lock);
		when(multiSearcher.getSearchables()).thenReturn(searchables);

		closed = close.execute(indexContext);
		assertTrue(closed);

		indexContext.setMultiSearcher(multiSearcher);
		when(lock.isLocked()).thenReturn(Boolean.TRUE);

		closed = close.execute(indexContext);
		assertTrue(closed);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);
		assertFalse(serverIndexDirectory.exists());
		assertFalse(anotherServerIndexDirectory.exists());
	}

}
