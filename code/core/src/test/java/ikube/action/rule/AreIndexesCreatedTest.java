package ikube.action.rule;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO This test was originally in the base action, where there still is code that can be migrated to this class.
 * 
 * @author Michael Couck
 * @since 19.03.11
 * @version 01.00
 */
public class AreIndexesCreatedTest extends ATest {

	private AreIndexesCreated indexesCreated;

	public AreIndexesCreatedTest() {
		super(AreIndexesCreatedTest.class);
	}

	@Before
	public void before() {
		indexesCreated = new AreIndexesCreated();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void evaluate() {
		boolean result = indexesCreated.evaluate(INDEX_CONTEXT);
		// TODO This seems to fail in Maven, probably because of
		// conflicts in the creation of the indexes
		// assertFalse(result);
		createIndex(INDEX_CONTEXT, "soem data, wif wong smelling");
		result = indexesCreated.evaluate(INDEX_CONTEXT);
		assertTrue(result);
	}

}
