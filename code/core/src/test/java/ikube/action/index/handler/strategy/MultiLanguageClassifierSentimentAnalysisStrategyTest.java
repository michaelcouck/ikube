package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

@Deprecated
public class MultiLanguageClassifierSentimentAnalysisStrategyTest extends AbstractTest {

	private MultiLanguageClassifierSentimentAnalysisStrategy sentimentAnalysisStrategy;

	@Before
	public void before() {
		sentimentAnalysisStrategy = new MultiLanguageClassifierSentimentAnalysisStrategy();
		sentimentAnalysisStrategy.initialize();
	}

	@Test
	public void aroundProcess() throws Exception {
		Indexable<?> indexable = Mockito.mock(Indexable.class);
		Mockito.when(indexable.getContent()).thenReturn("What a lovely day");
		Document document = new Document();
		IndexManager.addStringField(IConstants.LANGUAGE, "en", document, Store.YES, Index.ANALYZED, TermVector.NO);
		IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.CATEGORIES[0], document, Store.YES, Index.ANALYZED, TermVector.NO);
		Object resource = new Object();
		sentimentAnalysisStrategy.aroundProcess(indexContext, indexable, document, resource);
	}

}
