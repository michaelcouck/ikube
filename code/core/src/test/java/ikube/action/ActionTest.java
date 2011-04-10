package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.search.Searchable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test can be spit up to test the individual rules.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ActionTest extends ATest {

	private transient final Action<IndexContext, Boolean> action = new Action<IndexContext, Boolean>() {
		@Override
		public Boolean execute(final IndexContext indexContext) {
			return Boolean.FALSE;
		}
	};

	public ActionTest() {
		super(ActionTest.class);
	}
	
	@Before
	public void before() {
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}
	
	@After
	public void after() throws Exception {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void indexCurrent() throws InterruptedException {
		String serverIndexDirectoryPath = getServerIndexDirectoryPath(INDEX_CONTEXT);
		File serverIndexDirectory = FileUtilities.getFile(serverIndexDirectoryPath, Boolean.TRUE);

		long maxAge = 10;
		when(INDEX_CONTEXT.getMaxAge()).thenReturn(maxAge);
		Thread.sleep(maxAge * 100);

		boolean indexCurrent = action.isIndexCurrent(INDEX_CONTEXT);
		assertFalse("The index should be out of date : ", indexCurrent);

		when(INDEX_CONTEXT.getMaxAge()).thenReturn(maxAge * 1000);

		indexCurrent = action.isIndexCurrent(INDEX_CONTEXT);
		assertTrue("Index should be current : " + indexCurrent, indexCurrent);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		assertFalse("Server index directory should exist : ", serverIndexDirectory.exists());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void shoudReopen() {
		File baseIndexDirectory = FileUtilities.getFile(INDEX_CONTEXT.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		when(INDEX_CONTEXT.getIndex().getMultiSearcher()).thenReturn(null);

		boolean shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		// Searcher null in the context
		assertTrue("Should reopen as the searcher is null : ", shouldReopen /* && !baseIndexDirectory.exists() */);
		when(INDEX_CONTEXT.getIndex().getMultiSearcher()).thenReturn(MULTI_SEARCHER);

		// No SEARCHABLES in the searcher
		when(INDEX.getMultiSearcher()).thenReturn(MULTI_SEARCHER);
		when(MULTI_SEARCHER.getSearchables()).thenReturn(new Searchable[0]);
		shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		assertTrue("Should reopen as there are no searchables in the searcher : ", shouldReopen);

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(INDEX_CONTEXT);
		File serverIndexDirectory = null;
		try {
			serverIndexDirectory = createIndex(new File(serverIndexDirectoryPath));
		} catch (Exception e) {
			logger.error("Exception creating the index : ", e);
		}

		when(FS_DIRECTORY.getFile()).thenReturn(new File(serverIndexDirectory.getAbsolutePath()));
		when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);

		// All the directories are in the searcher
		shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		assertFalse("All the indexes are up to date so we shouldn't reopen : ", shouldReopen);

		// Create a new server index directory
		File anotherServerIndexDirectory = null;
		try {
			anotherServerIndexDirectory = createIndex(new File(serverIndexDirectoryPath.replace(IP, "127.0.0.2")));
		} catch (Exception e) {
			logger.error("Exception creating the index : ", e);
		}

		shouldReopen = action.shouldReopen(INDEX_CONTEXT);
		assertTrue("Should reopen as there is a new index : ", shouldReopen);

		when(INDEX.getMultiSearcher()).thenReturn(null);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);

		assertFalse("Index directory should have been deleted : ", serverIndexDirectory.exists());
		assertFalse("Index directory should have been deleted : ", anotherServerIndexDirectory.exists());
	}

}