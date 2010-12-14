package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ActionTest extends BaseActionTest {

	private Action action = new Action() {
		@Override
		public Boolean execute(IndexContext e) {
			return Boolean.FALSE;
		}
	};

	@Test
	public void indexCurrent() throws Exception {
		long newMaxAge = 1000;
		long maxAge = indexContext.getMaxAge();
		indexContext.setMaxAge(newMaxAge);
		Thread.sleep(newMaxAge);

		boolean indexCurrent = action.isIndexCurrent(indexContext);
		assertFalse(indexCurrent);

		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(indexContext.getName());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(ip);
		File serverIndexDirectory = FileUtilities.getFile(builder.toString(), Boolean.TRUE);

		indexContext.setMaxAge(maxAge);

		indexCurrent = action.isIndexCurrent(indexContext);
		assertTrue(indexCurrent);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		assertFalse(serverIndexDirectory.exists());
	}

	@Test
	public void shoudReopen() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		boolean shouldReopen = action.shouldReopen(indexContext);
		// Searcher null in the context
		assertTrue(shouldReopen /* && !baseIndexDirectory.exists() */);

		indexContext.setMultiSearcher(multiSearcher);

		// No searchables in the searcher
		shouldReopen = action.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(indexContext.getName());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(ip);
		File serverIndexDirectory = createIndex(new File(builder.toString()));

		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.directory()).thenReturn(fsDirectory);
		when(fsDirectory.getFile()).thenReturn(new File(serverIndexDirectory.getAbsolutePath()));
		when(multiSearcher.getSearchables()).thenReturn(searchables);

		// All the directories are in the searcher
		shouldReopen = action.shouldReopen(indexContext);
		assertFalse(shouldReopen);

		// Create a new server index directory
		File anotherServerIndexDirectory = createIndex(new File(builder.toString().replace(ip, "127.0.0.2")));

		shouldReopen = action.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		indexContext.setMultiSearcher(null);
		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);

		assertFalse(serverIndexDirectory.exists());
		assertFalse(anotherServerIndexDirectory.exists());
	}

}