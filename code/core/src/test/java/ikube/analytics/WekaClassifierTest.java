package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 08.09.13
 * @version 01.00
 */
public class WekaClassifierTest extends AbstractTest {

	@Test
	public void classify() throws Exception {
		final WekaClassifier wekaClassifier = new WekaClassifier();
		wekaClassifier.initialize();
		wekaClassifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
		wekaClassifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
		wekaClassifier.build();

		String positive = wekaClassifier.classify(IConstants.POSITIVE);
		assertEquals(IConstants.POSITIVE, positive);
		String negative = wekaClassifier.classify(IConstants.NEGATIVE);
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

		positive = wekaClassifier.classify("Wonderful dinner on the beach");
		assertEquals(IConstants.POSITIVE, positive);
		negative = wekaClassifier.classify("Terrible pain all over");
		assertEquals(IConstants.NEGATIVE, negative);

		File directory = FileUtilities.findDirectoryRecursively(new File("."), 2, "txt_sentoken");
		logger.info("Directory : " + directory);
	}

}
