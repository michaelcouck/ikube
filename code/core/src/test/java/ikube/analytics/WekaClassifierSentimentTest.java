package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.Timer;

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
public class WekaClassifierSentimentTest extends AbstractTest {

	private WekaClassifierSentiment wekaClassifier;

	@Before
	public void before() {
		wekaClassifier = new WekaClassifierSentiment();
	}

	@Test
	@Ignore
	public void classifyFromFile() throws Exception {
		// wekaClassifier.file("instance.arff");
		wekaClassifier.init(null);
		wekaClassifier.build(null);
		classify(wekaClassifier);
		final int maxFiles = 50;
		double duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {
				try {
					trainOnSentimentData(wekaClassifier, maxFiles);
				} catch (Exception e) {
					logger.error(null, e);
				}
			}
		});
		logger.warn("Training : " + maxFiles + ", duration : " + duration);
	}

	private void classify(final WekaClassifierSentiment wekaClassifier) throws Exception {
		String positive = wekaClassifier.analyze(IConstants.POSITIVE);
		assertEquals(IConstants.POSITIVE, positive);
		String negative = wekaClassifier.analyze(IConstants.NEGATIVE);
		assertEquals(IConstants.NEGATIVE, negative);

		wekaClassifier.train(IConstants.POSITIVE, "Hello darling, what a wonderful dinner! :)");
		wekaClassifier.train(IConstants.NEGATIVE, "What a terrible pain I have in the neck. :(");
		wekaClassifier.build(null);

		positive = wekaClassifier.analyze("Wonderful dinner on the beach");
		assertEquals(IConstants.POSITIVE, positive);
		negative = wekaClassifier.analyze("Terrible pain all over");
		assertEquals(IConstants.NEGATIVE, negative);
	}

	void trainOnSentimentData(final WekaClassifierSentiment wekaClassifier, final int maxFiles) throws Exception {
		File directory = FileUtilities.findDirectoryRecursively(new File("."), 2, "txt_sentoken");
		File positiveDirectory = FileUtilities.findDirectoryRecursively(directory, "pos");
		File negativeDirectory = FileUtilities.findDirectoryRecursively(directory, "neg");
		trainOnSentimentData(wekaClassifier, IConstants.POSITIVE, maxFiles, positiveDirectory.listFiles());
		trainOnSentimentData(wekaClassifier, IConstants.NEGATIVE, maxFiles, negativeDirectory.listFiles());
	}

	void trainOnSentimentData(final WekaClassifierSentiment wekaClassifier, final String clazz, int maxFiles, final File[] files) throws Exception {
		logger.info("Files size : " + files.length);
		int totalLines = 0;
		int doneFiles = 0;
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
				logger.info("Done files : " + doneFiles + ", lines : " + lines.size() + ", total lines/instances : " + totalLines);
			}
		}
	}

	@Test
	public void classify() throws Exception {
		wekaClassifier.init(null);
		wekaClassifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
		wekaClassifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
		wekaClassifier.build(null);
		classify(wekaClassifier);
		// trainOnSentimentData(wekaClassifier);
	}

	@Test
	public void performance() {
		wekaClassifier.init(null);
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