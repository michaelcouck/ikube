package ikube.index.handler.internet.crawler;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class UrlPageHandlerTest extends BaseTest {

	private IndexableInternet indexableInternet;
	private IndexableInternetHandler handler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
	private IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);

	public UrlPageHandlerTest() {
		super(UrlPageHandlerTest.class);
	}

	@Before
	public void before() {
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		indexableInternet = ApplicationContextManager.getBean("internet");
		handler = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.clear(IConstants.URL);
		clusterManager.clear(IConstants.URL_DONE);
		clusterManager.clear(IConstants.URL_HASH);
	}

	@After
	public void after() {
		indexContext.getIndex().setIndexWriter(null);
		clusterManager.clear(IConstants.URL);
		clusterManager.clear(IConstants.URL_DONE);
		clusterManager.clear(IConstants.URL_HASH);
	}

	@Test
	public void run() throws Exception {
		Url url = new Url();
		url.setId(HashUtilities.hash(indexableInternet.getUrl()));
		url.setUrl(indexableInternet.getUrl());

		UrlPageHandler urlPageHandler = new UrlPageHandler(clusterManager, handler, indexableInternet);
		clusterManager.set(IConstants.URL, url.getId(), url);
		Thread thread = new Thread(urlPageHandler);
		thread.start();
		ThreadUtilities.waitForThreads(Arrays.asList(thread));
		// Verify that there are urls in the database, that they are all indexed and there are no duplicates
		assertTrue(clusterManager.get(Url.class, IConstants.URL, null, null, Integer.MAX_VALUE).size() == 0);
		assertTrue(clusterManager.get(Url.class, IConstants.URL_DONE, null, null, Integer.MAX_VALUE).size() > 0);
		assertTrue(clusterManager.get(Url.class, IConstants.URL_DONE, null, null, Integer.MAX_VALUE).size() >= clusterManager.get(
				Url.class, IConstants.URL_HASH, null, null, Integer.MAX_VALUE).size());
	}

	@Test
	public void performance() {
		int[] iterations = new int[] { 1000, 10000 };
		for (int i = 0; i < iterations.length; i++) {
			clusterManager.clear(IConstants.URL);
			clusterManager.clear(IConstants.URL_DONE);
			clusterManager.clear(IConstants.URL_HASH);
			performance(iterations[i]);
		}
	}

	@Test
	public void getContentFromUrl() {
		// HttpClient, IndexableInternet, Url
		// TODO Implement me!
	}

	@Test
	public void getParsedContent() {
		// Url, ByteOutputStream
		// TODO Implement me!
	}

	@Test
	public void addDocumentToIndex() {
		// IndexableInternet, Url, String
		// TODO Implement me!
	}

	@Test
	public void getUrlId() {
		// IndexableInternet, Url
		// TODO Implement me!
	}

	@Test
	public void extractLinksFromContent() {
		// IndexableInternet, Url, InputStream
		// TODO Implement me!
	}

	protected void performance(int iterations) {
		for (int i = 0; i < iterations; i++) {
			Url url = new Url();
			url.setId(new Long(i));
			clusterManager.set(IConstants.URL, url.getId(), url);
		}
		long id = iterations / 2 - (iterations / 4) - (iterations / 7);
		final Url url = new Url();
		url.setId(id);
		double iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				clusterManager.get(IConstants.URL, url.getId());
			}
		}, "Page handler set performance : " + iterations + " : ", iterations);
		assertTrue(iterationsPerSecond > 1000);
	}

}