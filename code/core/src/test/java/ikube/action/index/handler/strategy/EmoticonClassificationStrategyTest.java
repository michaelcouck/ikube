package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.toolkit.PERFORMANCE;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

public class EmoticonClassificationStrategyTest extends AbstractTest {

	private EmoticonClassificationStrategy emoticonClassificationStrategy;

	@Before
	public void before() {
		emoticonClassificationStrategy = new EmoticonClassificationStrategy();
		emoticonClassificationStrategy.initialize();
	}

	@Test
	public void aroundProcess() throws Exception {
		final Indexable indexable = Mockito.mock(Indexable.class);
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

		double executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws Throwable {
                emoticonClassificationStrategy.aroundProcess(indexContext, indexable, new Document(), resource);
            }
        }, "Emoticon strategy : ", 1000, Boolean.TRUE);
		assertTrue(executionsPerSecond > 100);
	}

}
