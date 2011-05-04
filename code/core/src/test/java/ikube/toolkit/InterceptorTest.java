package ikube.toolkit;

import ikube.BaseTest;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.model.IndexableTable;
import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is an ad -hoc test to see if the interceptors are in fact intercepting the classes, i.e. a configuration test more than anything
 * else.
 * 
 * @author Michael Couck
 * 
 */
public class InterceptorTest extends BaseTest {

	@Cascading
	private Document document;

	public InterceptorTest() {
		super(InterceptorTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks();
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		indexContext.getIndex().setIndexWriter(null);
	}

	@Test
	public void entichmentInterceptor() throws Exception {
		IndexableTableHandler tableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);
		IndexableTable indexableTable = ApplicationContextManager.getBean("addressTableH2");
		tableHandler.addDocument(indexContext, indexableTable, document);
		// TODO Verify somehow that the class is being intercepted
	}

}
