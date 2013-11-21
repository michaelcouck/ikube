package ikube.action.index.handler.strategy;

import static ikube.action.index.IndexManager.addStringField;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.WekaClassifierSentiment;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

public class ClassificationStrategyTest extends AbstractTest {

	private Map<String, String[]> inputs;
	private DynamicallyTrainedLanguageSpecificClassificationStrategy dynamicallyTrainedLanguageSpecificClassificationStrategy;

	@Before
	public void before() throws Exception {
		dynamicallyTrainedLanguageSpecificClassificationStrategy = new DynamicallyTrainedLanguageSpecificClassificationStrategy();
		WekaClassifierSentiment wekaClassifier = new WekaClassifierSentiment();
		wekaClassifier.init(null);
		dynamicallyTrainedLanguageSpecificClassificationStrategy.setClassifier(wekaClassifier);
		dynamicallyTrainedLanguageSpecificClassificationStrategy.initialize();

		inputs = new HashMap<String, String[]>();
		inputs.put(IConstants.POSITIVE, new String[] { "Lovely day", "Perfect and healthy", "Funny and amusing", "Great!" });
		inputs.put(IConstants.NEGATIVE, new String[] { "Not well", "Feeling sick", "Unhappy and miserable", "Loose your mind" });
	}

	@Test
	public void aroundProcess() throws Exception {
		// ThreadUtilities.sleep(3000);
		final Indexable<?> indexableColumn = new IndexableColumn();
		indexableColumn.setContent("Perfect day");

		// Strangely enough everything is classified as positive at this point, why?
		Document document = new Document();
		document = addStringField(IConstants.LANGUAGE, "en", indexableColumn, document);
		dynamicallyTrainedLanguageSpecificClassificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));

		// Now we train the classifier and re-test the category
		int maxTraining = 300;
		do {
			for (final String category : IConstants.CATEGORIES) {
				for (final String content : inputs.get(category)) {
					dynamicallyTrainedLanguageSpecificClassificationStrategy.train(category, content);
				}
			}
		} while (maxTraining-- > 0);

		indexableColumn.setContent("Perfect day");
		document = new Document();
		document = addStringField(IConstants.LANGUAGE, "en", indexableColumn, document);
		dynamicallyTrainedLanguageSpecificClassificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));

		indexableColumn.setContent("Not well");
		document = new Document();
		document = addStringField(IConstants.LANGUAGE, "en", indexableColumn, document);
		dynamicallyTrainedLanguageSpecificClassificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		assertEquals(IConstants.NEGATIVE, document.get(IConstants.CLASSIFICATION));

		// We add the positive field for classification and the strategy should also classify the data as positive and there should be no conflict field
		// document = addStringField(IConstants.LANGUAGE, "en", document, Store.YES, Index.ANALYZED, TermVector.YES);
		document = addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableColumn, document);
		dynamicallyTrainedLanguageSpecificClassificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		// Static classification of positive
		assertEquals(IConstants.NEGATIVE + " " + IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				dynamicallyTrainedLanguageSpecificClassificationStrategy.aroundProcess(indexContext, indexableColumn, new Document(), null);
			}
		}, "Classification strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 100);

		// Check the memory for a volume trainings
		PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				dynamicallyTrainedLanguageSpecificClassificationStrategy.train(IConstants.POSITIVE,
						"This is a small one hunded and fourty character piece of positive text");
			}
		}, "Classification training strategy : ", 1000, Boolean.TRUE);
	}

	@Test
	public void threadTraining() {
		ThreadUtilities.initialize();
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < 100; i++) {
			class Trainer implements Runnable {
				public void run() {
					int training = 1000;
					do {
						for (final String category : IConstants.CATEGORIES) {
							for (final String content : inputs.get(category)) {
								dynamicallyTrainedLanguageSpecificClassificationStrategy.train(category, content);
							}
						}
					} while (training-- > 0);
				}
			}
			Future<?> future = ThreadUtilities.submitSystem(new Trainer());
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

}