package ikube.interceptor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.IndexReaderMock;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 19.01.12
 * @version 01.00
 */
public class ActionInterceptorTest extends ATest {

	@Cascading
	private Document document;
	private ActionInterceptor actionInterceptor;
	private IndexableFileSystem indexableFileSystem;
	private ProceedingJoinPoint proceedingJoinPoint;

	public ActionInterceptorTest() {
		super(ActionInterceptorTest.class);
	}

	@Before
	public void before() throws Exception {
		actionInterceptor = new ActionInterceptor();
		indexableFileSystem = mock(IndexableFileSystem.class);
		proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		List<Indexable<?>> children = new ArrayList<Indexable<?>>(Arrays.asList(indexableFileSystem));

		when(proceedingJoinPoint.getArgs()).thenReturn(new Object[] { indexContext });
		when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
		when(indexContext.getChildren()).thenReturn(children);
		when(indexReader.numDocs()).thenReturn(3);

		Mockit.setUpMocks(IndexReaderMock.class);
		IndexReaderMock.INDEX_READER = indexReader;
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void preProcess() throws Throwable {
		actionInterceptor.preProcess(proceedingJoinPoint);
		verify(indexContext, atLeastOnce()).setHashes(anyList());
	}

	@Test
	public void postProcess() throws Throwable {
		List<Long> hashes = new ArrayList<Long>(Arrays.asList(3472321447647299525l));
		when(indexContext.getHashes()).thenReturn(hashes);
		actionInterceptor.postProcess(proceedingJoinPoint);
		verify(indexWriter, atLeastOnce()).deleteDocuments(any(Term.class));
	}

}