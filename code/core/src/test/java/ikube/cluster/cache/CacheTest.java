package ikube.cluster.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.PerformanceTester;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
public class CacheTest extends ATest {

	private transient Url				url;
	private transient CacheHazelcast	cache;

	public CacheTest() {
		super(CacheTest.class);
	}

	@Before
	public void before() {
		cache = new CacheHazelcast();
		cache.initialise();
		url = new Url();
		url.setUrl("http://localhost");
		url.setHash(System.nanoTime());
	}

	@After
	public void after() {
		cache.clear(Url.class.getName());
	}

	@Test
	public void setNameIdObjectGetNameIdRemoveNameId() {
		// get(final String name, final Long id)
		Url url = new Url();
		long id = System.currentTimeMillis();
		url.setId(id);
		// First set the url in the cache
		cache.set(Url.class.getName(), id, url);
		// Now check that is there
		url = cache.get(Url.class.getName(), id);
		assertNotNull("We should have the url from the cache : ", url);
		// Check that it is not in the server map
		url = cache.get(Server.class.getName(), id);
		assertNull("The url should be null because it is the wrong map : ", url);
		// Remove the url from the cache
		cache.remove(Url.class.getName(), id);
		url = cache.get(Url.class.getName(), id);
		assertNull("The url should be gone from the cache : ", url);
	}

	@Test
	public void getNameCriteriaActionSize() throws Exception {
		// <T extends Object> List<T>
		// final String name, final ICriteria<T> criteria, final IAction<T> action, final int size
		int size = 100;
		for (int i = 0; i < size; i++) {
			Url url = new Url();
			url.setHash(System.nanoTime());
			url.setUrl(Integer.toHexString(i));
			cache.set(Url.class.getName(), url.getHash(), url);
			Thread.sleep(1);
		}
		ICriteria<Url> criteria = new ICriteria<Url>() {
			@Override
			public boolean evaluate(Url url) {
				return url != null;
			}
		};
		IAction<Url> action = new IAction<Url>() {
			@Override
			public void execute(Url object) {
				// Do nothing
			}
		};
		List<Url> batchUrls = cache.get(Url.class.getName(), criteria, action, 100);
		assertEquals(size, batchUrls.size());
		cache.clear(Url.class.getName());
	}

	@Test
	public void getNameSqlClearName() {
		// <T extends Object> T
		// final String name, final String sql
		Url url = new Url();
		long id = System.currentTimeMillis();
		url.setId(id);
		cache.set(Url.class.getName(), id, url);
		url = cache.get(Url.class.getName(), "id = " + url.getId());
		assertNotNull("We should get the url with the id specified : ", url);
		// final String name
		int size = cache.size(Url.class.getName());
		assertEquals("There should be only one url in the cache : ", 1, size);
		cache.clear(Url.class.getName());
		size = cache.size(Url.class.getName());
		assertEquals("There should be no urls in the cache : ", 0, size);
	}

	@Test
	public void performance() {
		int iterations = 10;
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Exception {
				long nanoTime = System.nanoTime();
				Url url = new Url();
				url.setHash(nanoTime);
				url.setUrl(Long.toHexString(nanoTime));
				cache.set(Url.class.getName(), url.getHash(), url);
			}
		}, "Cache set : ", iterations);
		assertTrue(executionsPerSecond > 100);
	}

}