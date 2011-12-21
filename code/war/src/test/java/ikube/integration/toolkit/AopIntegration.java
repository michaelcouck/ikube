package ikube.integration.toolkit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.integration.AbstractIntegration;
import ikube.model.Execution;
import ikube.model.Index;
import ikube.model.IndexContext;
import ikube.monitoring.IMonitoringInterceptor;
import ikube.search.Search;
import ikube.search.SearchSingle;
import ikube.service.ISearcherWebService;
import ikube.toolkit.ApplicationContextManager;

import java.util.Map;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * NOTE This test must not run in the integration batch because Mockit will crash the test.
 * 
 * This test is to see if the intercepter is in fact intercepting the classes that it should. Difficult to automate this test of course so
 * it is a manual test. We could of course write an intercepter for the intercepter and verify that the intercepter is called by Spring,
 * personally I think that is a little over kill.
 * 
 * @author Michael Couck
 * @since 30.04.2011
 * @version 01.00
 */
public class AopIntegration extends AbstractIntegration {

	@MockClass(realClass = Search.class, inverse = true)
	public class SearchMock {
	}

	@MockClass(realClass = SearchSingle.class, inverse = true)
	public class SearchSingleMock {
	}

	@Cascading
	private Document document;
	@Cascading
	private MultiSearcher multiSearcher;

	private Index index;
	private IndexWriter indexWriter;
	private IndexContext<?> indexContext;
	/** Class under test. */
	private IMonitoringInterceptor interceptor;

	@Before
	public void before() {
		Mockit.setUpMocks(SearchSingleMock.class);
		index = Mockito.mock(Index.class);
		indexWriter = Mockito.mock(IndexWriter.class);
		indexContext = Mockito.mock(IndexContext.class);
		when(indexContext.getIndex()).thenReturn(index);
		when(index.getIndexWriter()).thenReturn(indexWriter);
		interceptor = ApplicationContextManager.getBean(IMonitoringInterceptor.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void addDocument() throws Exception {
		Map<String, Execution> indexingExecutions = Deencapsulation.getField(interceptor, "indexingExecutions");
		assertTrue(indexingExecutions.size() == 0);
		IndexableTableHandler indexableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);
		indexableHandler.addDocument(indexContext, document);
		logger.info("Executions : " + indexingExecutions);
		verify(indexWriter, Mockito.atLeastOnce()).addDocument(document);
		assertTrue(indexingExecutions.size() > 0);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void search() throws Exception {
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> context : indexContexts.values()) {
			context.getIndex().setMultiSearcher(multiSearcher);
		}
		Map<String, Execution> searchingExecutions = Deencapsulation.getField(interceptor, "searchingExecutions");
		assertTrue(searchingExecutions.size() == 0);
		ISearcherWebService searcherWebService = ApplicationContextManager.getBean(ISearcherWebService.class);
		searcherWebService.searchSingle(IConstants.GEOSPATIAL, "hello world", "name", Boolean.TRUE, 0, Integer.MAX_VALUE);
		assertTrue(searchingExecutions.size() > 0);
	}

}