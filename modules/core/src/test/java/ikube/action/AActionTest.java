package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;

public class AActionTest extends BaseActionTest {

	private AAction<IndexContext, Boolean> aAction = new AAction<IndexContext, Boolean>() {
		@Override
		public Boolean execute(IndexContext e) {
			return null;
		}
	};

	@Test
	public void indexCurrent() throws Exception {
		long newMaxAge = 1000;
		long maxAge = indexContext.getMaxAge();
		indexContext.setMaxAge(newMaxAge);
		Thread.sleep(newMaxAge);

		boolean indexCurrent = aAction.isIndexCurrent(indexContext);
		assertFalse(indexCurrent);

		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(indexContext.getName());

		File contextIndexDirectory = FileUtilities.getFile(builder.toString(), Boolean.TRUE);

		indexCurrent = aAction.isIndexCurrent(indexContext);
		assertTrue(indexCurrent);
		indexContext.setMaxAge(maxAge);

		FileUtilities.deleteFile(contextIndexDirectory, 1);
		assertFalse(contextIndexDirectory.exists());
	}

	@Test
	public void shoudReopen() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		boolean shouldReopen = aAction.shouldReopen(indexContext);
		// Searcher null in the context
		assertTrue(shouldReopen /* && !baseIndexDirectory.exists() */);

		indexContext.setMultiSearcher(multiSearcher);

		// No searchables in the searcher
		shouldReopen = aAction.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(ip);
		builder.append(File.separator);
		builder.append(indexContext.getName());
		File contextIndexDirectory = createIndex(new File(builder.toString()));

		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.directory()).thenReturn(fsDirectory);
		when(fsDirectory.getFile()).thenReturn(new File(contextIndexDirectory.getAbsolutePath()));
		when(multiSearcher.getSearchables()).thenReturn(searchables);

		// All the directories are in the searcher
		shouldReopen = aAction.shouldReopen(indexContext);
		assertFalse(shouldReopen);

		// Create a new server index directory
		File anotherContextIndexDirectory = createIndex(new File(builder.toString().replace(indexContext.getName(), "anotherContextIndex")));

		shouldReopen = aAction.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		indexContext.setMultiSearcher(null);
		FileUtilities.deleteFile(contextIndexDirectory, 1);
		FileUtilities.deleteFile(anotherContextIndexDirectory, 1);

		assertFalse(contextIndexDirectory.exists());
		assertFalse(anotherContextIndexDirectory.exists());
	}

}