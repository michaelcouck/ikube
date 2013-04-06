package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class AreDirectoriesEqualTest extends AbstractTest {

	private AreDirectoriesEqual directoriesEqual;

	public AreDirectoriesEqualTest() {
		super(AreDirectoriesEqualTest.class);
	}

	@Before
	public void before() {
		directoriesEqual = new AreDirectoriesEqual();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void evaluate() {
		File fileOne = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
		File fileTwo = FileUtilities.getFile(indexDirectoryPathBackup, Boolean.TRUE);
		boolean result = directoriesEqual.evaluate(new File[] { fileOne, fileTwo });
		assertFalse(result);

		fileTwo = FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);
		result = directoriesEqual.evaluate(new File[] { fileOne, fileTwo });
		assertTrue(result);
	}

}
