package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

// @Ignore
public class AActionTest extends BaseActionTest {

	private AAction<IndexContext, Boolean> aAction;

	@Before
	public void before() {
		this.aAction = new AAction<IndexContext, Boolean>() {
			@Override
			public Boolean execute(IndexContext e) {
				return null;
			}
		};
	}

	@Test
	public void indexCurrent() throws Exception {
		long newMaxAge = 1000;
		long maxAge = indexContext.getMaxAge();
		indexContext.setMaxAge(newMaxAge);
		Thread.sleep(newMaxAge);

		boolean indexCurrent = aAction.isIndexCurrent(indexContext);
		assertFalse(indexCurrent);

		String indexDirectory = new StringBuilder(indexContext.getIndexDirectoryPath()).append(File.separator).append(
				System.currentTimeMillis()).append(File.separator).append(indexContext.getServerName()).toString();
		File currentIndexDirectory = FileUtilities.getFile(indexDirectory, Boolean.TRUE);
		currentIndexDirectory.mkdirs();

		indexCurrent = aAction.isIndexCurrent(indexContext);
		assertTrue(indexCurrent);
		indexContext.setMaxAge(maxAge);

		FileUtilities.deleteFile(currentIndexDirectory, 1);
	}

	@Test
	public void shoudReopen() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		boolean shouldReopen = aAction.shouldReopen(indexContext);
		assertFalse(shouldReopen && !baseIndexDirectory.exists());

		File latestIndexDirectory = FileUtilities.getFile(baseIndexDirectory.getAbsolutePath() + File.separator
				+ Long.toString(System.currentTimeMillis()), Boolean.TRUE);

		shouldReopen = aAction.shouldReopen(indexContext);
		assertFalse(shouldReopen);

		String filePath = latestIndexDirectory.getAbsolutePath() + File.separatorChar + indexContext.getServerName();
		File serverIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);

		shouldReopen = aAction.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		indexContext.setMultiSearcher(multiSearcher);

		createIndex(latestIndexDirectory, indexContext.getServerName());

		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.directory()).thenReturn(fsDirectory);
		when(fsDirectory.getFile()).thenReturn(new File(serverIndexDirectory.getAbsolutePath()));
		when(multiSearcher.getSearchables()).thenReturn(searchables);

		shouldReopen = aAction.shouldReopen(indexContext);
		assertFalse(shouldReopen);

		// Create a new server index directory
		createIndex(latestIndexDirectory, "anotherServerIndex");

		shouldReopen = aAction.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		indexContext.setMultiSearcher(null);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

}