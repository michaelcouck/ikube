package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test is for the {@link IsNewIndexCreated} class that will check if there is a newer index than the one that is opened in the index
 * searcher.
 * 
 * @author Michael Couck
 * @since 11.06.11
 * @version 01.00
 */
public class IsNewIndexCreatedTest extends ATest {

	private String originalDirectoryPath;

	public IsNewIndexCreatedTest() {
		super(IsNewIndexCreatedTest.class);
	}

	@Before
	public void before() {
		originalDirectoryPath = INDEX_CONTEXT.getIndexDirectoryPath();
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./" + getClass().getSimpleName());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn(originalDirectoryPath);
	}

	@Test
	public void evaluate() throws Exception {
		// final IndexContext<?> indexContext
		IsNewIndexCreated isNewIndexCreated = new IsNewIndexCreated();
		boolean result = isNewIndexCreated.evaluate(INDEX_CONTEXT);
		assertFalse("There is no index created : ", result);

		File indexDirectory = createIndex(INDEX_CONTEXT, "Some data : ");

		result = isNewIndexCreated.evaluate(INDEX_CONTEXT);
		assertTrue("The index is created : ", result);

		// Open the index searcher on the latest index
		File serverIndexDirectory = new File(indexDirectory, IP);
		when(FS_DIRECTORY.getFile()).thenReturn(serverIndexDirectory);
		result = isNewIndexCreated.evaluate(INDEX_CONTEXT);
		assertFalse("The latest index is already opened : ", result);

		indexDirectory = createIndex(INDEX_CONTEXT, "Some more data : ");
		serverIndexDirectory = new File(indexDirectory, IP);
		result = isNewIndexCreated.evaluate(INDEX_CONTEXT);
		assertTrue("There is a new index, and the searcher is open on the old one : ", result);
	}

}