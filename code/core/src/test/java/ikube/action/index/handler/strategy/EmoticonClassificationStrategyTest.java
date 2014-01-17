package ikube.action.index.handler.strategy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;
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
		when(indexable.isStored()).thenReturn(Boolean.TRUE);
		when(indexable.isAnalyzed()).thenReturn(Boolean.TRUE);
		final Object resource = new Object();

		Document document = new Document();
		Mockito.when(indexable.getContent()).thenReturn("What a lovely day :) :)");
		emoticonClassificationStrategy.aroundProcess(indexContext, indexable, document, resource);
		assertEquals(IConstants.POSITIVE, document.get(IConstants.CLASSIFICATION));

		document = new Document();
		Mockito.when(indexable.getContent()).thenReturn("I am having a terrible time :( D:<");
		emoticonClassificationStrategy.aroundProcess(indexContext, indexable, document, resource);
		assertEquals(IConstants.NEGATIVE, document.get(IConstants.CLASSIFICATION));

		document = new Document();
		Mockito.when(indexable.getContent()).thenReturn("The bigger the better");
		emoticonClassificationStrategy.aroundProcess(indexContext, indexable, document, resource);
		assertNull(document.get(IConstants.CLASSIFICATION));

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				emoticonClassificationStrategy.aroundProcess(indexContext, indexable, new Document(), resource);
			}
		}, "Emoticon strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 100);
	}

}
