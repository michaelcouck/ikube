package ikube.search.spelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class CheckerExtTest extends ATest {

	private CheckerExt	checkerExt	= CheckerExt.getCheckerExt();

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

	@Test
	@Ignore
	public void dictionaries() throws Exception {
		File file = new File("C:/Users/Administrator/Downloads/Wordlist.Hacker.Cracker.multilanguage.list.over.10.million.words.dictionary.text.txt");
		FileInputStream fileInputStream = new FileInputStream(file);
		FileOutputStream fileOutputStream = new FileOutputStream(FileUtilities.getFile("./output.txt", Boolean.FALSE));
		int read = -1;
		byte[] bytes = new byte[1024];
		while ((read = fileInputStream.read(bytes)) > -1) {
			String string = new String(bytes, 0, read, IConstants.ENCODING);
			fileOutputStream.write(bytes, 0, read);
			logger.error(string);
			fileInputStream.skip(1000000);
		}
	}

}