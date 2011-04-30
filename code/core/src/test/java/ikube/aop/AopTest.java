package ikube.aop;

import ikube.BaseTest;
import ikube.action.Index;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.toolkit.ApplicationContextManager;

import java.io.IOException;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test is to see if the interceptor is in fact intercepting the classes that it should. Difficult to automate this test of course so
 * it is a manual test. We could of course write an interceptor for the interceptor and verify that the interceptor is called by Spring,
 * personally I think that is a little over kill.
 * 
 * @author Michael Couck
 * @since 30.04.2011
 * @version 01.00
 */
@Ignore
public class AopTest extends BaseTest {

	@Cascading
	private Document document;

	public AopTest() {
		super(AopTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void rule() throws Exception {
		Index index = ApplicationContextManager.getBean(Index.class);
		index.execute(indexContext);
	}

	@Test
	public void enrichment() throws CorruptIndexException, IOException {
		IndexableInternetHandler indexableHandler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		indexableHandler.addDocument(INDEX_CONTEXT, INDEXABLE, document);
	}

	@Test
	public void monitoring() {
		// TODO Implement me
	}

}
