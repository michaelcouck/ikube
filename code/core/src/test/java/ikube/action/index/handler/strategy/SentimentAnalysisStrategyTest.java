package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SentimentAnalysisStrategyTest extends AbstractTest {

	private SentimentAnalysisStrategy sentimentAnalysisStrategy;

	@Before
	public void before() {
		sentimentAnalysisStrategy = new SentimentAnalysisStrategy();
		sentimentAnalysisStrategy.initialize();
	}

	@Test
	public void aroundProcess() throws Exception {
		Indexable<?> indexable = Mockito.mock(Indexable.class);
		Mockito.when(indexable.getContent()).thenReturn("What a lovely day");
		Document document = new Document();
		Object resource = new Object();
		sentimentAnalysisStrategy.aroundProcess(indexContext, indexable, document, resource);
	}

}
