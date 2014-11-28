package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FILE;

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

	@Before
	public void before() {
		directoriesEqual = new AreDirectoriesEqual();
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void evaluate() {
		File fileOne = FILE.getFile(indexDirectoryPath, Boolean.TRUE);
		File fileTwo = FILE.getFile(indexDirectoryPathBackup, Boolean.TRUE);
		boolean result = directoriesEqual.evaluate(new File[] { fileOne, fileTwo });
		assertFalse(result);

		fileTwo = FILE.getFile(indexDirectoryPath, Boolean.TRUE);
		result = directoriesEqual.evaluate(new File[] { fileOne, fileTwo });
		assertTrue(result);
	}

}
