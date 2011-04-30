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
import org.junit.Test;

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
