package ikube.action.index.handler.strategy;

import static ikube.action.index.IndexManager.addStringField;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.junit.After;
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
		inputs.put(IConstants.CATEGORIES[0], new String[] { "Lovely day", "Perfect and healthy", "Funny and amusing", "Great!" });
		inputs.put(IConstants.CATEGORIES[1], new String[] { "Not well", "Feeling sick", "Unhappy and miserable", "Loose your mind" });
		inputs.put(IConstants.CATEGORIES[2], new String[] { "Before breakfast", "After lunch", "During dinner", "Sleep time" });
	}

	@After
	public void after() {
		File classifiersDirectory = classificationStrategy.getClassifiersDirectory();
		FileUtilities.deleteFile(classifiersDirectory);
	}

	@Test
	public void aroundProcess() throws Exception {
		ThreadUtilities.sleep(3000);
		final Indexable<?> indexableColumn = new IndexableColumn();
		indexableColumn.setContent("Perfect day");

		// We add a positive field for classification, and the strategy should add the conflict firld for neutral
		Document document = addStringField(IConstants.CLASSIFICATION, IConstants.CATEGORIES[0], new Document(), Store.YES, Index.ANALYZED, TermVector.YES);
		classificationStrategy.aroundProcess(indexContext, indexableColumn, document, null);
		logger.info("Document : " + document);
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));
		assertEquals(IConstants.NEUTRAL, document.get(IConstants.CLASSIFICATION_CONFLICT));

		// Now we train the classifier and re-test the category
		int epochs = 0;
		do {
			for (final String category : IConstants.CATEGORIES) {
				for (final String content : inputs.get(category)) {
					classificationStrategy.train(category, content);
					epochs++;
					if (epochs % 1000 == 0) {
						classificationStrategy.openClassifierOnCorpus();
					}
				}
			}
		} while (epochs < 3000);

		// We add the positive field for classification and the strategy should also classify the data as positive and there whould be not conflict field
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
		}, "Emoticon strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 1000);
	}

}