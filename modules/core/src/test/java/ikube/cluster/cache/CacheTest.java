package ikube.cluster.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.cluster.cache.Cache;
import ikube.model.Url;
import ikube.toolkit.PerformanceTester;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CacheTest extends ATest {

	private Url url;
	private Cache cache;
	private List<Thread> threads;

	@Before
	public void before() {
		cache = new Cache();
		url = new Url();
		url.setUrl("http://localhost");
		url.setHash(new Long(hashCode()));
		threads = new ArrayList<Thread>();
	}

	@After
	public void after() {
		cache = null;
	}

	@Test
	public void setUrl() {
		cache.setUrl(url);

		List<Url> batchUrls = cache.getUrlBatch(threads);
		assertTrue(batchUrls.size() == 1);

		cache.setUrl(url);
		batchUrls = cache.getUrlBatch(threads);
		assertTrue(batchUrls.size() == 0);
	}

	@Test
	public void getUrlWithHash() {
		Url hashUrl = cache.getUrlWithHash(url);
		assertNull(hashUrl);

		hashUrl = cache.getUrlWithHash(url);
		assertNotNull(hashUrl);
	}

	@Test
	public void getUrlBatch() {
		int urls = 100;
		for (int i = 0; i < urls; i++) {
			Url url = new Url();
			url.setHash(System.nanoTime());
			url.setUrl(Integer.toHexString(i));
			cache.setUrl(url);
		}
		List<Url> batchUrls = cache.getUrlBatch(threads);
		assertTrue(batchUrls.size() == urls);
	}

	@Test
	public void performance() throws Exception {
		int iterations = 100000;
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Exception {
				long nanoTime = System.nanoTime();
				Url url = new Url();
				url.setHash(nanoTime);
				url.setUrl(Long.toHexString(nanoTime));
				cache.setUrl(url);
			}
		}, "JGroupsCache set : ", iterations);
		assertTrue(executionsPerSecond > 1000);
	}

}