package ikube.integration.database.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.integration.AbstractIntegration;
import ikube.model.Action;
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

/**
 * This test is just to see that the database is persisting and removing entities correctly.
 * 
 * @author Michael Couck
 * @since long time
 * @version 01.00
 */
public class DataBaseIntegration extends AbstractIntegration {

	private IDataBase dataBase;

	@Before
	public void before() {
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		delete(dataBase, Url.class, File.class, Action.class);
	}

	@After
	public void after() {
		delete(dataBase, Url.class, File.class, Action.class);
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
		long hash = System.nanoTime();
		Url url = new Url();
		url.setHash(hash);
		dataBase.persist(url);
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
	public void performance() throws Exception {
		final int iterations = 3;
		final int batchSize = 100;
		double minimumInsertsPerSecond = 10d;
		for (int i = 0; i < iterations; i++) {
			double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Throwable {
					final List<Url> urls = getUrls(batchSize);
					dataBase.persistBatch(urls);
					urls.clear();
				}
			}, "Iterations per second : ", iterations);
			double insertsPerSecond = (perSecond * batchSize);
			logger.info("Inserts per second : " + insertsPerSecond);
			assertTrue("We must have at least " + minimumInsertsPerSecond + " inserts per second : " + insertsPerSecond,
					insertsPerSecond > minimumInsertsPerSecond);
			delete(dataBase, Url.class);
		}
	}

	@Test
	public void executeQuery() {
		Long count = dataBase.execute(Long.class, Action.SELECT_FROM_ACTIONS_COUNT);
		assertNotNull("The count should never be null : ", count);

		List<Action> actions = Arrays.asList(new Action(), new Action(), new Action());
		dataBase.persistBatch(actions);

		count = dataBase.execute(Long.class, Action.SELECT_FROM_ACTIONS_COUNT);
		assertNotNull("The count should never be null : ", count);
		assertEquals("The count should be the size of the url list : ", Long.valueOf(actions.size()), count);
	}

	@Test
	public void findClassSortFieldsDirectionOfSortFirstMaxResults() throws Exception {
		int totalUrlInserted = 10;
		List<Url> urls = getUrls(totalUrlInserted);
		dataBase.persistBatch(urls);
		List<Url> dbUrls = dataBase.find(Url.class, new String[] { "urlId" }, new boolean[] { false }, 0, Integer.MAX_VALUE);
		logger.info("Urls : " + dbUrls.size());
		long previousId = Long.MAX_VALUE;
		for (Url url : dbUrls) {
			logger.info("Url : " + url.getId() + ", " + url.getHash());
			assertTrue("The ids must be in descending order : ", previousId > url.getId());
			previousId = url.getId();
		}

		int firstResult = totalUrlInserted / 4;
		int maxResults = totalUrlInserted / 2;
		dbUrls = dataBase.find(Url.class, new String[] { "urlId" }, new boolean[] { false }, firstResult, maxResults);
		assertEquals("Max results should be half the total : ", maxResults, dbUrls.size());
		previousId = Long.MAX_VALUE;
		for (Url url : dbUrls) {
			logger.info("Url : " + url.getId() + ", " + url.getHash());
			assertTrue("The ids must be in descending order : ", previousId > url.getId());
		}
	}

	@Test
	public void findClassStringNamesValues() throws Exception {
		List<Url> urls = getUrls(1);
		dataBase.persistBatch(urls);
		String[] names = new String[] { IConstants.NAME };
		Object[] values = new Object[] { "index" };
		Url url = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME, names, values);
		assertNotNull("There should be at least one url with this name : ", url);
	}

	@Test
	public void findClassStringNamesValuesStartMax() throws Exception {
		int inserted = 90;
		List<Url> urls = getUrls(inserted);
		dataBase.persistBatch(urls);
		String[] names = new String[] { IConstants.NAME };
		Object[] values = new Object[] { "index" };
		List<Url> dbUrls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME, names, values, 0, Integer.MAX_VALUE);
		assertNotNull(dbUrls);
		assertTrue(dbUrls.size() > 0);
		assertEquals(dbUrls.size(), inserted);
		
		int fetched = inserted / 3;
		dbUrls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME, names, values, 0, fetched);
		assertNotNull(dbUrls);
		assertTrue(dbUrls.size() > 0);
		assertEquals(dbUrls.size(), fetched);
		
		int started = 10;
		dbUrls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME, names, values, started, fetched);
		assertNotNull(dbUrls);
		assertTrue(dbUrls.size() > 0);
		assertEquals(dbUrls.size(), Math.min(inserted - started, fetched));
		
		started = 80;
		dbUrls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME, names, values, started, fetched);
		assertNotNull(dbUrls);
		assertTrue(dbUrls.size() > 0);
		assertEquals(dbUrls.size(), inserted - started);
	}

	protected List<Url> getUrls(int batchSize) throws Exception {
		List<Url> urls = new ArrayList<Url>();
		long hash = System.currentTimeMillis();
		for (int i = 0; i < batchSize; i++) {
			Url url = new Url();
			url.setName("index");
			url.setParsedContent("parsed content");
			url.setRawContent(new byte[0]);
			url.setTitle("title");
			url.setUrl("url");
			url.setUrlId(hash++);
			url.setHash(hash++);
			url.setIndexed(Boolean.FALSE);
			url.setContentType("content type");
			urls.add(url);
		}
		return urls;
	}

}
