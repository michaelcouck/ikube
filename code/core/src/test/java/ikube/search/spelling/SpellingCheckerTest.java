package ikube.search.spelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.mock.SpellingCheckerMock;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class SpellingCheckerTest extends ATest {

	private SpellingChecker spellingChecker;

	public SpellingCheckerTest() {
		super(SpellingCheckerTest.class);
	}

	@AfterClass
	public static void afterClass() {
		FileUtilities.deleteFile(new File("./spellingIndex"), 1);
	}

	@Before
	public void before() throws Exception {
		Mockit.tearDownMocks(SpellingChecker.class);

		spellingChecker = new SpellingChecker();
		File languagesWordFileDirectory = FileUtilities.findFileRecursively(new File("."), "english.txt").getParentFile();
		Deencapsulation.setField(spellingChecker, "languageWordListsDirectory", languagesWordFileDirectory.getAbsolutePath());
		Deencapsulation.setField(spellingChecker, "spellingIndexDirectoryPath", "./spellingIndex");
		spellingChecker.initialize();
	}

	@After
	public void after() {
		// Mockit.setUpMock(SpellingCheckerMock.class);
	}

	@Test
	public void checkWords() {
		String wrong = "wrongk";
		String correct = "wrongs";
		String corrected = spellingChecker.checkWords(wrong);
		logger.info("Corrected words : " + corrected);
		assertEquals(correct, corrected);

		String phraseWrong = wrong + " AND " + wrong;
		corrected = spellingChecker.checkWords(phraseWrong);
		logger.info("Corrected words : " + corrected);
		assertEquals(correct + " AND " + correct, corrected);

		String right = "AND there are AND some words WITH AND another";
		corrected = spellingChecker.checkWords(right);
		logger.info("Corrected words : " + corrected);
		assertEquals("AND there are AND some words AND another", corrected);
	}

	@Test
	public void checkPerformance() {
		double iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				spellingChecker.checkWords("michael");
			}
		}, "Spelling checking performance : ", 1000, Boolean.FALSE);
		assertTrue(iterationsPerSecond > 100);
		iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				spellingChecker.checkWords("couck");
			}
		}, "Spelling checking performance : ", 1000, Boolean.FALSE);
		assertTrue(iterationsPerSecond > 100);
	}

}