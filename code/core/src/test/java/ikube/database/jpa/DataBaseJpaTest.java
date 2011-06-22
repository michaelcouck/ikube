package ikube.database.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.File;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.PerformanceTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataBaseJpaTest extends BaseTest {

	private IDataBase dataBase;

	public DataBaseJpaTest() {
		super(DataBaseJpaTest.class);
	}

	@Before
	public void before() {
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		delete(dataBase, Url.class, File.class);
	}

	@After
	public void after() {
		delete(dataBase, Url.class, File.class);
	}

	@Test
	public void allOperations() {
		// Persist
		Url url = dataBase.persist(new Url());
		// Find long
		Object object = dataBase.find(Url.class, url.getId());
		assertNotNull("The url should have been persisted : ", object);

		// Merge
		long hash = System.nanoTime();
		url.setHash(hash);
		dataBase.merge(url);
		url = dataBase.find(Url.class, url.getId());
		assertEquals("The hash should have been updated : ", hash, url.getHash());

		// Find class long
		url = dataBase.find(Url.class, url.getId());
		assertNotNull("The url should have been persisted : ", url);

		// Find int int
		List<Url> urls = dataBase.find(Url.class, 0, 100);
		assertEquals("There should be one url in the database : ", 1, urls.size());

		// Remove
		dataBase.remove(Url.class, url.getId());
		url = dataBase.find(Url.class, url.getId());
		assertNull("The url should have been removed : ", url);

		// Remove T
		url = new Url();
		url = dataBase.persist(url);
		assertNotNull("The url should have been persisted : ", url);
		dataBase.remove(url);
		url = dataBase.find(Url.class, url.getId());
		assertNull("The url should have been deleted : ", url);

		// Remove String
		url = dataBase.persist(new Url());
		int removed = dataBase.remove(Url.DELETE_ALL_URLS);
		assertEquals("The url should have been removed : ", 1, removed);
		url = dataBase.find(Url.class, url.getId());
		assertNull("The url should have been deleted : ", url);
	}

	@Test
	public void findClassStringMapIntInt() {
		// Find class string map int int
		long hash = System.nanoTime();
		Url url = new Url();
		url.setHash(hash);
		url = dataBase.persist(url);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.HASH, hash);
		List<Url> urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_HASH, parameters, 0, 100);
		assertEquals("There should be one url in the database : ", 1, urls.size());
	}

	@Test
	public void persistRemoveBatch() {
		List<Url> urls = Arrays.asList(new Url(), new Url(), new Url());
		dataBase.persistBatch(urls);
		List<Url> dbUrls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be a list of urls in the database : ", urls.size(), dbUrls.size());

		dataBase.removeBatch(urls);
		dbUrls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be no urls in the database : ", 0, dbUrls.size());
	}

	@Test
	public void performance() {
		final int iterations = 10;
		final int batchSize = 1000;
		for (int i = 0; i < iterations; i++) {
			PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Throwable {
					List<Url> urls = getUrls(batchSize);
					dataBase.persistBatch(urls);
				}
			}, "Insert : ", iterations);
		}
	}

	private List<Url> getUrls(int batchSize) {
		List<Url> urls = new ArrayList<Url>();
		for (int i = 0; i < batchSize; i++) {
			long hash = System.nanoTime();
			Url url = new Url();
			url.setHash(hash);
			url.setIndexed(Boolean.FALSE);
			// url.setUrl(Long.toString(hash));
			urls.add(url);
		}
		return urls;
	}

}
