package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EmoticonSentimentAnalysisStrategyTest extends AbstractTest {

	private EmoticonSentimentAnalysisStrategy emoticonSentimentAnalysisStrategy;

	@Before
	public void before() {
		emoticonSentimentAnalysisStrategy = new EmoticonSentimentAnalysisStrategy();
		emoticonSentimentAnalysisStrategy.initialize();
	}

	@Test
	public void aroundProcess() throws Exception {
		Indexable<?> indexable = Mockito.mock(Indexable.class);
		Document document = new Document();
		Object resource = new Object();

		Mockito.when(indexable.getContent()).thenReturn("What a lovely day :) giggle");
		emoticonSentimentAnalysisStrategy.aroundProcess(indexContext, indexable, document, resource);
		logger.info("Document : " + document);

		Mockito.when(indexable.getContent()).thenReturn("I am having a terrible time :( cry");
		emoticonSentimentAnalysisStrategy.aroundProcess(indexContext, indexable, document, resource);
		logger.info("Document : " + document);
	}

}
