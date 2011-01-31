package ikube.database.odb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.Url;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DataBaseOdbTest extends ATest {

	private static DataBaseOdb DATA_BASE;
	private static String DATA_BASE_FILE = "ikube.test.odb";

	@BeforeClass
	public static void beforeClass() {
		FileUtilities.deleteFiles(new File("."), DATA_BASE_FILE);
		DATA_BASE = new DataBaseOdb();
		@SuppressWarnings("unused")
		List<Index> indexes = Arrays.asList(
		//
				new Index(Url.class.getName(), Arrays.asList(IConstants.ID)),
				// new Index(Url.class.getName(), Arrays.asList(IConstants.URL)),
				new Index(Url.class.getName(), Arrays.asList(IConstants.HASH)));
		// DATA_BASE.setIndexes(indexes);
		DATA_BASE.initialise(DATA_BASE_FILE);
	}

	@AfterClass
	public static void afterClass() {
		DATA_BASE.close();
		FileUtilities.deleteFiles(new File("."), DATA_BASE_FILE);
	}

	@Test
	public void getIdField() {
		// Class<?>
		Field field = DatabaseUtilities.getIdField(IndexableColumn.class, null);
		assertNotNull(field);
		assertEquals("id", field.getName());
	}

	@Test
	public void getIdFieldName() {
		// Class<?>
		String fieldName = DatabaseUtilities.getIdFieldName(IndexableColumn.class);
		assertNotNull(fieldName);
		assertEquals("id", fieldName);
	}

	@Test
	public void getIdFieldValue() {
		// Class<?>
		Long id = System.currentTimeMillis();
		Url token = new Url();
		token.setId(id);
		Object idFieldValue = DatabaseUtilities.getIdFieldValue(token);
		assertNotNull(idFieldValue);
		assertEquals(id, idFieldValue);
	}

	@Test
	public void setIdField() {
		// T, long
		Indexable<IndexableColumn> indexable = new IndexableColumn();
		DatabaseUtilities.setIdField(indexable, Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, indexable.getId());
	}

	@Test
	public void persist() {
		Indexable<?> indexable = new IndexableColumn();
		indexable.setId(System.currentTimeMillis());
		DATA_BASE.persist(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.ID, indexable.getId());
		Indexable<?> found = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNotNull(found);

		DATA_BASE.remove(indexable);
		found = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNull(found);
	}

	@Test
	public void findLong() {
		Url url = new Url();
		url.setId(System.nanoTime());
		DATA_BASE.persist(url);

		Object dataBaseObject = DATA_BASE.find(url.getId());
		assertNotNull(dataBaseObject);
	}

	@Test
	public void findClassLong() {
		// Class<T>, Long
		Url url = new Url();
		url.setId(System.nanoTime());
		DATA_BASE.persist(url);

		Url dataBaseUrl = DATA_BASE.find(Url.class, url.getId());
		assertNotNull(dataBaseUrl);

		DATA_BASE.remove(url);
	}

	@Test
	public void removeClassLong() {
		// Class<T>, Long
		Url url = new Url();
		url.setId(System.nanoTime());
		DATA_BASE.persist(url);
		Url dataBaseUrl = DATA_BASE.find(Url.class, url.getId());
		assertNotNull(dataBaseUrl);

		DATA_BASE.remove(Url.class, url.getId());

		dataBaseUrl = DATA_BASE.find(Url.class, url.getId());
		assertNull(dataBaseUrl);
	}

	@Test
	public void merge() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		DATA_BASE.persist(indexable);

		indexable.setName(name);
		DATA_BASE.merge(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, name);
		Indexable<?> merged = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertEquals(indexable.getName(), merged.getName());

		DATA_BASE.remove(indexable);
		Indexable<?> found = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNull(found);
	}

	@Test
	public void remove() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		indexable.setName(name);
		DATA_BASE.persist(indexable);
		DATA_BASE.remove(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, name);
		Indexable<?> removed = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNull(removed);
	}

	@Test
	public void findClassParametersUnique() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		indexable.setName(name);

		DATA_BASE.persist(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, name);
		Indexable<?> persisted = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNotNull(persisted);

		DATA_BASE.remove(persisted);
		persisted = DATA_BASE.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNull(persisted);
	}

	@Test
	public void findClassFirstMax() {
		for (int i = 0; i < 100; i++) {
			Url token = new Url();
			token.setId(i);
			DATA_BASE.persist(token);
		}

		int first = 0;
		int max = 10;
		List<Url> firstResults = DATA_BASE.find(Url.class, first, max);
		assertEquals(max, firstResults.size());

		first = 10;
		max = 50;
		List<Url> secondResults = DATA_BASE.find(Url.class, first, max);
		assertEquals(max - first, secondResults.size());

		first = 90;
		max = 100;
		List<Url> thirdResults = DATA_BASE.find(Url.class, first, max);
		assertEquals(max - first, thirdResults.size());

		assertFalse(firstResults.removeAll(secondResults));
		assertFalse(secondResults.removeAll(thirdResults));
	}

	@Test
	public void findClassParametersFirstMax() {
		String urlString = "localhost";
		for (int i = 0; i < 100; i++) {
			Url url = new Url();
			url.setId(System.nanoTime());
			url.setUrl(urlString);
			DATA_BASE.persist(url);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.URL, urlString);
		int first = 0;
		int max = 10;
		List<Url> firstResults = DATA_BASE.find(Url.class, parameters, first, max);
		assertEquals(max, firstResults.size());

		first = 10;
		max = 50;
		List<Url> secondResults = DATA_BASE.find(Url.class, parameters, first, max);
		assertEquals(max - first, secondResults.size());

		first = 90;
		max = 100;
		List<Url> thirdResults = DATA_BASE.find(Url.class, parameters, first, max);
		assertEquals(max - first, thirdResults.size());

		assertFalse(firstResults.removeAll(secondResults));
		assertFalse(secondResults.removeAll(thirdResults));
	}

	@Test
	public void performance() throws Exception {
		int iterations = 100;

		final List<Url> tokens = new ArrayList<Url>();
		for (int i = 0; i < iterations; i++) {
			Url url = new Url();
			url.setId(System.nanoTime());
			tokens.add(url);
			Thread.sleep(1);
		}

		// Persist
		@SuppressWarnings("unused")
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			Iterator<Url> iterator = tokens.iterator();

			@Override
			public void execute() throws Exception {
				DATA_BASE.persist(iterator.next());
			}
		}, "Database persist performance : ", iterations);
		// assertTrue(executionsPerSecond > 1000);

		// Find
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {

			Iterator<Url> iterator = tokens.iterator();
			Map<String, Object> parameters = new HashMap<String, Object>();

			@Override
			public void execute() throws Exception {
				Url token = iterator.next();
				parameters.put(IConstants.ID, token.getId());
				DATA_BASE.find(token.getClass(), parameters, Boolean.TRUE);
			}
		}, "Database find performance : ", iterations);
		// assertTrue(executionsPerSecond > 1000);

		// Merge
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			Iterator<Url> iterator = tokens.iterator();

			@Override
			public void execute() throws Exception {
				Url token = iterator.next();
				DATA_BASE.merge(token);
			}
		}, "Database merge performance : ", iterations);
		// assertTrue(executionsPerSecond > 10);

		// Remove
		DATA_BASE.deleteIndex(Url.class);
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			Iterator<Url> iterator = tokens.iterator();

			@Override
			public void execute() throws Exception {
				try {
					DATA_BASE.remove(iterator.next());
				} catch (Exception e) {
					throw e;
				}
			}
		}, "Database remove performance : ", iterations);
		// assertTrue(executionsPerSecond > 10);
	}

	@Test
	public void threading() throws Exception {
		final int iterations = 100;
		int threadCount = 3;
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					String localhost = null;
					long id = 0;
					for (int i = 0; i < iterations; i++) {
						double random = Math.random();
						if (random < 0.25) {
							// Persist
							Url token = new Url();
							localhost = "localhost." + System.nanoTime();
							token.setUrl(localhost);
							id = System.nanoTime();
							token.setId(id);
							log("Persisting : ", i, token);
							DATA_BASE.persist(token);
						} else if (random >= 0.25 && random < 0.5) {
							// Find
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(IConstants.ID, id);
							log("Finding : ", i, null);
							DATA_BASE.find(Url.class, parameters, Boolean.FALSE);
						} else if (random >= 5 && random < 0.75) {
							// Merge
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(IConstants.URL, localhost);
							Url token = DATA_BASE.find(Url.class, parameters, Boolean.FALSE);
							if (token != null) {
								token.setUrl(localhost + ".duplicate");
							}
							DATA_BASE.merge(token);
						} else {
							// Remove
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(IConstants.URL, localhost);
							Url token = DATA_BASE.find(Url.class, parameters, Boolean.FALSE);
							if (token != null) {
								log("Removing : ", i, token);
								DATA_BASE.remove(token);
							}
						}
					}
				}

				protected void log(String action, int iteration, Url token) {
					if (iteration % 100 == 0) {
						logger.debug(Logging.getString("Action : ", action, iteration, token));
					}
				}
			}, this.getClass().getSimpleName() + "." + i);
			threads.add(thread);
			thread.start();
		}
		ThreadUtilities.waitForThreads(threads);
		assertTrue(Boolean.TRUE);
	}

	@Test
	@Ignore
	public void defragment() {
		// Just add a few thousand objects and wait for the defragment
		int iterations = 11000;
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				Url url = new Url();
				url.setId(System.nanoTime());
				url.setUrl(Long.toHexString(System.nanoTime()));
				DATA_BASE.persist(url);
				// Thread.sleep(1);
			}
		}, "Database defragment : ", iterations);
		// TODO - check the database size
	}

	public static void main(String[] args) {
		String dataBaseFile = "./88034101776063.ikube.odb";

		DataBaseOdb dataBase = new DataBaseOdb();
		dataBase.initialise(dataBaseFile);
		List<Object> objects = dataBase.find(Object.class, 0, Integer.MAX_VALUE);
		for (Object object : objects) {
			System.out.println("Url : " + object);
		}
		dataBase.close();

		// ODB odb = ODBFactory.open(DATA_BASE_FILE);
		// odb.defragmentTo(DATA_BASE_FILE + ".defragmented");
		// odb.close();
	}

}