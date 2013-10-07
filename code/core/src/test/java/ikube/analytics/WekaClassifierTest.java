package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 08.09.13
 * @version 01.00
 */
public class WekaClassifierTest extends AbstractTest {

	@Test
	public void classify() throws Exception {
		WekaClassifier wekaClassifier = new WekaClassifier();
		wekaClassifier.initialize();
		wekaClassifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
		wekaClassifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
		wekaClassifier.build();
		classify(wekaClassifier);
		trainOnSentimentData(wekaClassifier);
	}

	@Test
	public void classifyFromFile() throws Exception {
		WekaClassifier wekaClassifier = new WekaClassifier();
		wekaClassifier.file("instance.arff");
		wekaClassifier.initialize();
		wekaClassifier.build();
		classify(wekaClassifier);
		trainOnSentimentData(wekaClassifier);
	}

	private void classify(final WekaClassifier wekaClassifier) throws Exception {
		String positive = wekaClassifier.analyze(IConstants.POSITIVE);
		assertEquals(IConstants.POSITIVE, positive);
		String negative = wekaClassifier.analyze(IConstants.NEGATIVE);
		assertEquals(IConstants.NEGATIVE, negative);

		int iterations = 501;
		double duration = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				wekaClassifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
				wekaClassifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
			}
		}, "SMO classification training : ", iterations, Boolean.TRUE);
		logger.info("Duration for " + iterations + " iterations : " + duration);

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
		for (final File file : files) {
			FileReader fileReader = new FileReader(file);
			List<String> lines = IOUtils.readLines(fileReader);
			totalLines += lines.size();
			logger.info("Lines : " + lines.size() + ", " + totalLines);
//			for (final String line : lines) {
//				wekaClassifier.train(clazz, line);
//			}
		}
	}

}