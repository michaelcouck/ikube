package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO Implement me!
 * 
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class IsIndexCurrentTest extends ATest {

	private static String latestIndexDirectory;

	@MockClass(realClass = FileUtilities.class)
	public static class FileUtilitiesMock {

		@Mock()
		public static synchronized File getLatestIndexDirectory(final String baseIndexDirectoryPath) {
			return new File(latestIndexDirectory);
		}
	}

	public IsIndexCurrentTest() {
		super(IsIndexCurrentTest.class);
	}

	@BeforeClass
	public static void beforeClass() {
		Mockit.setUpMock(FileUtilities.class, FileUtilitiesMock.class);
	}

	@AfterClass
	public static void afterClass() {
		Mockit.tearDownMocks(FileUtilities.class);
	}
	
	/** Class under test. */
	private IsIndexCurrent isIndexCurrentRule = new IsIndexCurrent();

	@Test
	public void evaluate() {
		String indexDirectory = "./indexes/index/";
		latestIndexDirectory = indexDirectory + (System.currentTimeMillis() - (1000 * 60 * 60 * 10));
		
		boolean isIndexCurrent = isIndexCurrentRule.evaluate(INDEX_CONTEXT);
		assertFalse(isIndexCurrent);

		latestIndexDirectory = indexDirectory + System.currentTimeMillis();
		isIndexCurrent = isIndexCurrentRule.evaluate(INDEX_CONTEXT);
		assertTrue(isIndexCurrent);
	}

}