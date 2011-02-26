package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.search.Searchable;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ActionTest extends ATest {

	private Action action = new Action() {
		@Override
		public Boolean execute(IndexContext e) {
			return Boolean.FALSE;
		}
	};
	
	@Before
	public void before() {
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./indexes");
		when(INDEX_CONTEXT.getIndexName()).thenReturn("actiontestindex");
		when(INDEX_CONTEXT.getIndex()).thenReturn(INDEX);
	}

	@Test
	public void indexCurrent() throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append(INDEX_CONTEXT.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(INDEX_CONTEXT.getIndexName());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(IP);
		File serverIndexDirectory = FileUtilities.getFile(builder.toString(), Boolean.TRUE);

		long maxAge = 10;
		when(INDEX_CONTEXT.getMaxAge()).thenReturn(maxAge);
		Thread.sleep(maxAge * 100);

		boolean indexCurrent = action.isIndexCurrent(INDEX_CONTEXT);
		assertFalse(indexCurrent);

		when(INDEX_CONTEXT.getMaxAge()).thenReturn(maxAge * 1000);

		indexCurrent = action.isIndexCurrent(INDEX_CONTEXT);
		assertTrue(indexCurrent);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		assertFalse(serverIndexDirectory.exists());
	}

	@Test
	public void shoudReopen() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(INDEX_CONTEXT.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		boolean shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		// Searcher null in the context
		assertTrue(shouldReopen /* && !baseIndexDirectory.exists() */);

		// No SEARCHABLES in the searcher
		when(INDEX.getMultiSearcher()).thenReturn(MULTI_SEARCHER);
		when(MULTI_SEARCHER.getSearchables()).thenReturn(new Searchable[0]);
		shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		assertTrue(shouldReopen);

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(INDEX_CONTEXT);
		File serverIndexDirectory = createIndex(new File(serverIndexDirectoryPath));

		when(FS_DIRECTORY.getFile()).thenReturn(new File(serverIndexDirectory.getAbsolutePath()));
		when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);

		// All the directories are in the searcher
		shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		assertFalse(shouldReopen);

		// Create a new server index directory
		File anotherServerIndexDirectory = createIndex(new File(serverIndexDirectoryPath.replace(IP, "127.0.0.2")));

		shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		assertTrue(shouldReopen);

		when(INDEX.getMultiSearcher()).thenReturn(null);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);

		assertFalse(serverIndexDirectory.exists());
		assertFalse(anotherServerIndexDirectory.exists());
	}

}