package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 08.09.13
 * @version 01.00
 */
public class WekaClassifierTest extends AbstractTest {

	private WekaClassifier wekaClassifier;

	@Before
	public void before() {
		wekaClassifier = new WekaClassifier();
	}

	@Test
	public void classifyFromFile() throws Exception {
		wekaClassifier.file("instance.arff");
		wekaClassifier.initialize();
		wekaClassifier.build();
		// trainOnSentimentData(wekaClassifier);
		classify(wekaClassifier);
	}

	private void classify(final WekaClassifier wekaClassifier) throws Exception {
		String positive = wekaClassifier.analyze(IConstants.POSITIVE);
		assertEquals(IConstants.POSITIVE, positive);
		String negative = wekaClassifier.analyze(IConstants.NEGATIVE);
		assertEquals(IConstants.NEGATIVE, negative);

		wekaClassifier.train(IConstants.POSITIVE, "Hello darling, what a wonderful dinner! :)");
		wekaClassifier.train(IConstants.NEGATIVE, "What a terrible pain I have in the neck. :(");
		wekaClassifier.build();

		positive = wekaClassifier.analyze("Wonderful dinner on the beach");
		assertEquals(IConstants.POSITIVE, positive);
		negative = wekaClassifier.analyze("Terrible pain all over");
		assertEquals(IConstants.NEGATIVE, negative);
	}

	void trainOnSentimentData(final WekaClassifier wekaClassifier) throws Exception {
		File directory = FileUtilities.findDirectoryRecursively(new File("."), 2, "txt_sentoken");
		File positiveDirectory = FileUtilities.findDirectoryRecursively(directory, "pos");
		File negativeDirectory = FileUtilities.findDirectoryRecursively(directory, "neg");
		trainOnSentimentData(wekaClassifier, IConstants.POSITIVE, positiveDirectory.listFiles());
		trainOnSentimentData(wekaClassifier, IConstants.NEGATIVE, negativeDirectory.listFiles());
	}

	void trainOnSentimentData(final WekaClassifier wekaClassifier, final String clazz, final File[] files) throws Exception {
		logger.info("Files size : " + files.length);
		int totalLines = 0;
		int doneFiles = 0;
		int maxFiles = 50;
		for (final File file : files) {
			if (maxFiles-- <= 0) {
				break;
			}
			FileReader fileReader = new FileReader(file);
			List<String> lines = IOUtils.readLines(fileReader);
			totalLines += lines.size();
			for (final String line : lines) {
				wekaClassifier.train(clazz, line);
			}
			doneFiles++;
			if (doneFiles % 10 == 0) {
				logger.info("Done files : " + doneFiles + ", lines : " + lines.size() + ", " + totalLines);
			}
		}
	}

	@Test
	@Ignore
	public void classify() throws Exception {
		wekaClassifier.initialize();
		wekaClassifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
		wekaClassifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
		wekaClassifier.build();
		// trainOnSentimentData(wekaClassifier);
		classify(wekaClassifier);
	}

	@Test
	@Ignore
	public void performance() {
		wekaClassifier.initialize();
		int iterations = 1001;
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				wekaClassifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
				wekaClassifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
			}
		}, "SMO classification training : ", iterations, Boolean.TRUE);
		assertTrue(executionsPerSecond > 1000);
	}

}