package ikube.search.spelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.PerformanceTester;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class CheckerExtTest extends ATest {

	private CheckerExt checkerExt = CheckerExt.getCheckerExt();

	public CheckerExtTest() {
		super(CheckerExtTest.class);
	}

	@Test
	public void checkWord() {
		String wrong = "wrongk";
		String correct = "wrongs";
		String corrected = checkerExt.checkWords(wrong);
		logger.info("Corrected words : " + corrected);
		assertEquals(correct, corrected);
	}

	@Test
	public void checkPerformance() {
		double iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				checkerExt.checkWords("michael");
			}
		}, "Spelling checking performance : ", 1000, Boolean.FALSE);
		assertTrue(iterationsPerSecond > 100);
		iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				checkerExt.checkWords("couck");
			}
		}, "Spelling checking performance : ", 1000, Boolean.FALSE);
		assertTrue(iterationsPerSecond > 100);
	}

}
