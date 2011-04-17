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
 * TODO Implement me!
 * 
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class AreUnopenedIndexesTest extends ATest {
	
	private AreUnopenedIndexes areUnopenedIndexes;

	public AreUnopenedIndexesTest() {
		super(AreUnopenedIndexesTest.class);
	}
	
	@Before
	public void before() {
		areUnopenedIndexes = new AreUnopenedIndexes();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}
	
	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void evaluate() {
		boolean result = areUnopenedIndexes.evaluate(INDEX_CONTEXT);
		assertFalse(result);
		
		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "some words to index");
		File serverIndexDirectory = new File(latestIndexDirectory, IP);
		result = areUnopenedIndexes.evaluate(INDEX_CONTEXT);
		assertTrue(result);
		
		when(FS_DIRECTORY.getFile()).thenReturn(serverIndexDirectory);
		result = areUnopenedIndexes.evaluate(INDEX_CONTEXT);
		assertFalse(result);
	}

}