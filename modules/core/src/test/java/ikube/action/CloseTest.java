package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import ikube.action.Close;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;



public class CloseTest extends BaseActionTest {

	private Close close = new Close();

	@Test
	public void execute() throws Exception {
		indexContext.setMultiSearcher(multiSearcher);
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		String filePath = baseIndexDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
		File latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		File serverIndexDirectory = createIndex(latestIndexDirectory, indexContext.getServerName());
		boolean closed = close.execute(indexContext);
		assertTrue(closed);

		serverIndexDirectory = createIndex(latestIndexDirectory, indexContext.getServerName() + "Again");
		indexContext.setMultiSearcher(multiSearcher);
		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.directory()).thenReturn(fsDirectory);
		when(lock.isLocked()).thenReturn(Boolean.FALSE);
		when(fsDirectory.getFile()).thenReturn(new File(serverIndexDirectory.getAbsolutePath()));
		when(fsDirectory.makeLock(anyString())).thenReturn(lock);
		when(multiSearcher.getSearchables()).thenReturn(searchables);

		closed = close.execute(indexContext);
		assertTrue(closed);

		indexContext.setMultiSearcher(multiSearcher);
		when(lock.isLocked()).thenReturn(Boolean.TRUE);

		closed = close.execute(indexContext);
		assertTrue(closed);

		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

}
