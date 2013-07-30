package ikube.action.index.handler.strategy;

import static ikube.action.index.IndexManager.addStringField;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
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
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.junit.Before;
import org.junit.Test;

public class ClassificationStrategyTest extends AbstractTest {

	private Map<String, String[]> inputs;
	private ClassificationStrategy classificationStrategy;

	@Before
	public void before() {
		classificationStrategy = new ClassificationStrategy();
		classificationStrategy.initialize();

		inputs = new HashMap<String, String[]>();
		inputs.put(IConstants.POSITIVE, new String[] { "Lovely day", "Perfect and healthy", "Funny and amusing", "Great!" });
		inputs.put(IConstants.NEGATIVE, new String[] { "Not well", "Feeling sick", "Unhappy and miserable", "Loose your mind" });
		inputs.put(IConstants.NEUTRAL, new String[] { "Before breakfast", "After lunch", "During dinner", "Sleep time" });
	}

	@Test
	public void aroundProcess() throws Exception {
		ThreadUtilities.sleep(3000);
		final Indexable<?> indexableColumn = new IndexableColumn();
		indexableColumn.setContent("Perfect day");

		Document document = new Document();
		classificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		assertEquals(IConstants.NEUTRAL, document.get(IConstants.CLASSIFICATION));

		// Now we train the classifier and re-test the category
		int maxTraining = 300;
		do {
			for (final String category : IConstants.CATEGORIES) {
				for (final String content : inputs.get(category)) {
					classificationStrategy.train(category, content);
				}
			}
		} while (maxTraining-- > 0);

		document = new Document();
		classificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));

		// We add the positive field for classification and the strategy should also classify the data as positive and there should be no conflict field
		document = addStringField(IConstants.CLASSIFICATION, IConstants.CATEGORIES[0], new Document(), Store.YES, Index.ANALYZED, TermVector.YES);
		classificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		// The classification should be positive
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));
		// The strategy asserts that the data is positive and there is no conflict field
		assertNull(document.get(IConstants.CLASSIFICATION_CONFLICT));

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				classificationStrategy.aroundProcess(indexContext, indexableColumn, new Document(), null);
			}
		}, "Classification strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 100);

		// Check the memory for a million trainings
		// PerformanceTester.execute(new PerformanceTester.APerform() {
		// public void execute() throws Throwable {
		// classificationStrategy.train(IConstants.NEUTRAL, "This is a small one hunded and fourty character piece of neutral text");
		// }
		// }, "Classification training strategy : ", 10000, Boolean.TRUE);
	}

	@Test
	public void threadTraining() {
		ThreadUtilities.initialize();
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < 10; i++) {
			class Trainer implements Runnable {
				public void run() {
					int training = 100;
					do {
						for (final String category : IConstants.CATEGORIES) {
							for (final String content : inputs.get(category)) {
								classificationStrategy.train(category, content);
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