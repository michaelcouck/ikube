package ikube.action.index.handler.strategy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.toolkit.PerformanceTester;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EmoticonClassificationStrategyTest extends AbstractTest {

	private EmoticonClassificationStrategy emoticonClassificationStrategy;

	@Before
	public void before() {
		emoticonClassificationStrategy = new EmoticonClassificationStrategy();
		emoticonClassificationStrategy.initialize();
	}

	@Test
	public void aroundProcess() throws Exception {
		final Indexable<?> indexable = Mockito.mock(Indexable.class);
		final Object resource = new Object();

		Document document = new Document();
		Mockito.when(indexable.getContent()).thenReturn("What a lovely day :)");
		emoticonClassificationStrategy.aroundProcess(indexContext, indexable, document, resource);
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));

		document = new Document();
		Mockito.when(indexable.getContent()).thenReturn("I am having a terrible time :(");
		emoticonClassificationStrategy.aroundProcess(indexContext, indexable, document, resource);
		assertEquals(IConstants.NEGATIVE, document.get(IConstants.CLASSIFICATION));

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				emoticonClassificationStrategy.aroundProcess(indexContext, indexable, new Document(), resource);
			}
		}, "Emoticon strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 1000);
	}

}
