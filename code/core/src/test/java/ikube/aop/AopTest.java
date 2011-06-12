package ikube.aop;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.action.Index;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.mock.AtomicActionMock;
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
import org.junit.Ignore;
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
public class AopTest extends ATest {

	@Cascading
	private Document document;

	public AopTest() {
		super(AopTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		Mockit.setUpMocks(AtomicActionMock.class);
		when(INDEX_CONTEXT.getIndexables()).thenAnswer(new Answer<List<Indexable<?>>>() {
			@Override
			public List<Indexable<?>> answer(InvocationOnMock invocation) throws Throwable {
				return Arrays.asList();
			}
		});
		// .thenCallRealMethod()
		INDEX_CONTEXT.setIndexName("indexName");
		INDEX_CONTEXT.setBufferedDocs(10);
		INDEX_CONTEXT.setMaxFieldLength(1000);
		INDEX_CONTEXT.setMergeFactor(100);
		INDEX_CONTEXT.setBufferSize(64);
		INDEX_CONTEXT.getIndex().setIndexWriter(INDEX_WRITER);
		INDEX_CONTEXT.setIndexDirectoryPath(this.indexDirectoryPath);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	@Ignore
	public void rule() throws Exception {
		AtomicActionMock.INVOCATIONS = 0;
		Index index = ApplicationContextManager.getBean(Index.class);
		index.execute(INDEX_CONTEXT);
		Thread.sleep(3000);
		assertTrue("There should be at least one invocation of getting the lock : ", AtomicActionMock.INVOCATIONS > 0);
		AtomicActionMock.INVOCATIONS = 0;

		// This doesn't work in the combined unit tests because there could
		// be a lock on the cluster from another object
		verify(INDEX_CONTEXT, Mockito.atLeastOnce()).getIndexables();
	}

	@Test
	public void enrichment() throws Exception {
		IndexableInternetHandler indexableHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		when(INDEXABLE.isAddress()).thenReturn(Boolean.FALSE);
		indexableHandler.addDocument(INDEX_CONTEXT, INDEXABLE, document);
		when(INDEXABLE.isAddress()).thenReturn(Boolean.TRUE);
		verify(INDEXABLE, Mockito.atLeastOnce()).isAddress();
	}

	@Test
	public void monitoring() {
		// TODO Implement me
	}

}