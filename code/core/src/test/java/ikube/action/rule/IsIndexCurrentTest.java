package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

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
public class IsIndexCurrentTest extends ATest {

	@MockClass(realClass = FileUtilities.class)
	public static class FileUtilitiesMock {

		private static File	LATEST_INDEX_DIRECTORY;

		@Mock()
		public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
			return LATEST_INDEX_DIRECTORY;
		}

		public static void setLatestIndexDirectory(File latestIndexDirectory) {
			LATEST_INDEX_DIRECTORY = latestIndexDirectory;
		}
	}

	/** Class under test. */
	private IsIndexCurrent	isIndexCurrentRule	= new IsIndexCurrent();

	public IsIndexCurrentTest() {
		super(IsIndexCurrentTest.class);
	}

	@Before
	public void beforeClass() {
		Mockit.setUpMock(FileUtilities.class, FileUtilitiesMock.class);
	}

	@After
	public void afterClass() {
		Mockit.tearDownMocks(FileUtilities.class);
	}

	@Test
	public void evaluate() {
		String indexDirectory = "./indexes/index/";
		File latestIndexDirectory = new File(indexDirectory + (System.currentTimeMillis() - (1000 * 60 * 60 * 10)));
		FileUtilitiesMock.setLatestIndexDirectory(latestIndexDirectory);

		boolean isIndexCurrent = isIndexCurrentRule.evaluate(indexContext);
		assertFalse(isIndexCurrent);

		latestIndexDirectory = new File(indexDirectory + System.currentTimeMillis());
		FileUtilitiesMock.setLatestIndexDirectory(latestIndexDirectory);
		isIndexCurrent = isIndexCurrentRule.evaluate(indexContext);
		assertTrue(isIndexCurrent);
	}

}