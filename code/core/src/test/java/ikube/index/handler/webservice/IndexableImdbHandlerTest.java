package ikube.index.handler.webservice;

import ikube.ATest;
import ikube.model.IndexableImdb;
import mockit.Cascading;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IndexableImdbHandlerTest extends ATest {

	@Cascading
	private IndexableImdb indexableImdb;
	private IndexableImdbHandler indexableImdbHandler;

	public IndexableImdbHandlerTest() {
		super(IndexableImdbHandlerTest.class);
	}

	@Before
	public void before() {
		// Mock not initialized here
		indexableImdbHandler = new IndexableImdbHandler();
		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void handle() throws Exception {
		// Is init here
		indexableImdbHandler.handle(indexContext, indexableImdb);
	}

}
