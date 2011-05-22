package ikube.aop;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.action.Index;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This test is to see if the intercepter is in fact intercepting the classes that it should. Difficult to automate this test of course so
 * it is a manual test. We could of course write an intercepter for the intercepter and verify that the intercepter is called by Spring,
 * personally I think that is a little over kill.
 * 
 * @author Michael Couck
 * @since 30.04.2011
 * @version 01.00
 */
public class AopTest extends BaseTest {

	@Cascading
	private Document document;
	private IndexContext indexContext = spy(new IndexContext());

	public AopTest() {
		super(AopTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		when(indexContext.getIndexables()).thenAnswer(new Answer<List<Indexable<?>>>() {
			@Override
			public List<Indexable<?>> answer(InvocationOnMock invocation) throws Throwable {
				return Arrays.asList();
			}
		}).thenCallRealMethod();
		indexContext.setIndexName("indexName");
		indexContext.setBufferedDocs(10);
		indexContext.setMaxFieldLength(1000);
		indexContext.setMergeFactor(100);
		indexContext.setBufferSize(64);
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		indexContext.setIndexDirectoryPath(this.indexDirectoryPath);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void rule() throws Exception {
		Index index = ApplicationContextManager.getBean(Index.class);
		index.execute(indexContext);
		verify(indexContext, Mockito.atLeastOnce()).getIndexables();
	}

	@Test
	public void enrichment() throws Exception {
		IndexableInternetHandler indexableHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		when(INDEXABLE.isAddress()).thenReturn(Boolean.FALSE);
		indexableHandler.addDocument(INDEX_CONTEXT, INDEXABLE, document);
		verify(INDEXABLE, Mockito.atLeastOnce()).isAddress();
		when(INDEXABLE.isAddress()).thenReturn(Boolean.TRUE);
	}

	@Test
	public void monitoring() {
		// TODO Implement me
	}

}