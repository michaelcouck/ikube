package ikube.database.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataBaseJpaTest extends BaseTest {

	@BeforeClass
	public static void beforeClass() {
		ApplicationContextManager.closeApplicationContext();
		Mockit.tearDownMocks(ApplicationContextManager.class);
		BaseTest.beforeClass();
	}

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
		double minimumInsertsPerSecond = 1000d;
		for (int i = 0; i < iterations; i++) {
			double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Throwable {
					List<Url> urls = getUrls(batchSize);
					dataBase.persistBatch(urls);
				}
			}, "Iterations per second : ", iterations);
			double insertsPerSecond = (perSecond * batchSize);
			logger.info("Inserts per second : " + insertsPerSecond);
			assertTrue("We must have at least " + minimumInsertsPerSecond + " inserts per second : ",
					insertsPerSecond > minimumInsertsPerSecond);
		}
	}

	private List<Url> getUrls(int batchSize) {
		List<Url> urls = new ArrayList<Url>();
		for (int i = 0; i < batchSize; i++) {
			Url url = new Url();
			url.setName("index");
			url.setParsedContent("parsed content");
			url.setRawContent(new byte[0]);
			url.setTitle("title");
			url.setUrl("url");
			url.setUrlId(System.nanoTime());
			url.setHash(System.nanoTime());
			url.setIndexed(Boolean.FALSE);
			url.setContentType("content type");
			urls.add(url);
		}
		return urls;
	}

}
