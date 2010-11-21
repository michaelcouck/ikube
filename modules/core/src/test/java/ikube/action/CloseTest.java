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

		File contextIndexDirectory = createIndex(new File(getContextIndexDirectoryPath(indexContext)));
		boolean closed = close.execute(indexContext);
		assertTrue(closed);

		File anotherContextIndexDirectory = createIndex(new File(getContextIndexDirectoryPath(indexContext).replace(indexContext.getName(),
				"anotherContext")));
		indexContext.setMultiSearcher(multiSearcher);
		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.directory()).thenReturn(fsDirectory);
		when(lock.isLocked()).thenReturn(Boolean.FALSE);
		when(fsDirectory.getFile()).thenReturn(new File(contextIndexDirectory.getAbsolutePath()));
		when(fsDirectory.makeLock(anyString())).thenReturn(lock);
		when(multiSearcher.getSearchables()).thenReturn(searchables);

		closed = close.execute(indexContext);
		assertTrue(closed);

		indexContext.setMultiSearcher(multiSearcher);
		when(lock.isLocked()).thenReturn(Boolean.TRUE);

		closed = close.execute(indexContext);
		assertTrue(closed);

		FileUtilities.deleteFile(contextIndexDirectory, 1);
		FileUtilities.deleteFile(anotherContextIndexDirectory, 1);
		assertFalse(contextIndexDirectory.exists());
		assertFalse(anotherContextIndexDirectory.exists());
	}

}
