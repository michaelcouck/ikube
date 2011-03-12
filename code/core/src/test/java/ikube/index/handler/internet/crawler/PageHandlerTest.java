package ikube.index.handler.internet.crawler;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.PerformanceTester;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class PageHandlerTest extends BaseTest {

	private IndexableInternet indexableInternet;

	@Before
	public void before() {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		indexableInternet = ApplicationContextManager.getBean("internet");
	}

	@After
	public void after() {
		indexContext.getIndex().setIndexWriter(null);
	}

	@Test
	public void run() throws Exception {
		Url url = new Url();
		url.setId(HashUtilities.hash(indexableInternet.getUrl()));
		url.setUrl(indexableInternet.getUrl());

		PageHandler pageHandler = new PageHandler(new ArrayList<Thread>());
		pageHandler.setIndexContext(indexContext);
		pageHandler.setIndexableInternet(indexableInternet);
		PageHandler.IN_SET.add(url);
		Thread thread = new Thread(pageHandler);
		thread.start();
		thread.join();
		// Verify that there are urls in the database, that they are all indexed and there are no duplicates
		assertTrue(PageHandler.IN_SET.size() == 0);
		assertTrue(PageHandler.OUT_SET.size() > 0);
		assertTrue(PageHandler.OUT_SET.size() >= PageHandler.HASH_SET.size());
	}

	@Test
	public void performance() {
		int[] iterations = new int[] { 1000, 10000, 100000 };
		for (int i = 0; i < iterations.length; i++) {
			PageHandler.IN_SET.clear();
			PageHandler.OUT_SET.clear();
			PageHandler.HASH_SET.clear();
			performance(iterations[i]);
		}
	}

	protected void performance(int iterations) {
		for (int i = 0; i < iterations; i++) {
			Url url = new Url();
			url.setId(i);
			PageHandler.setUrl(url);
		}
		long id = iterations / 2 - (iterations / 4) - (iterations / 7);
		final Url url = new Url();
		url.setId(id);
		double iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				PageHandler.exists(url);
			}
		}, "Page handler set performance : " + iterations + " : ", iterations);
		assertTrue(iterationsPerSecond > 1000);
	}

}