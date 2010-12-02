package ikube.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.Token;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DataBaseOdbTest extends ATest {

	private static DataBaseOdb dataBase;
	private static String dataBaseFile = "ikube.test.odb";

	@BeforeClass
	public static void beforeClass() {
		FileUtilities.deleteFiles(new File("."), dataBaseFile);
		dataBase = new DataBaseOdb();
		dataBase.initialise(dataBaseFile);
	}

	@AfterClass
	public static void afterClass() {
		dataBase.close();
		FileUtilities.deleteFiles(new File("."), dataBaseFile);
	}

	@Before
	public void before() {
		delete(dataBase, Indexable.class, IndexableColumn.class, Token.class);
	}

	@After
	public void after() {
		delete(dataBase, Indexable.class, IndexableColumn.class, Token.class);
	}

	@Test
	public void getIdField() {
		// Class<?>
		Field field = dataBase.getIdField(IndexableColumn.class, null);
		assertNotNull(field);
		assertEquals("id", field.getName());
	}

	@Test
	public void getIdFieldName() {
		// Class<?>
		String fieldName = dataBase.getIdFieldName(IndexableColumn.class);
		assertNotNull(fieldName);
		assertEquals("id", fieldName);
	}
	
	@Test
	public void getIdFieldValue() {
		// Class<?>
		Long id = System.currentTimeMillis();
		Token token = new Token();
		token.setId(id);
		Object idFieldValue = dataBase.getIdFieldValue(token);
		assertNotNull(idFieldValue);
		assertEquals(id, idFieldValue);
	}

	@Test
	public void setIdField() {
		// T, long
		Indexable<IndexableColumn> indexable = new IndexableColumn();
		dataBase.setIdField(indexable, Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, indexable.getId());
	}

	@Test
	public void persist() {
		Indexable<?> indexable = new IndexableColumn();
		indexable.setId(System.currentTimeMillis());
		dataBase.persist(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.ID, indexable.getId());
		Indexable<?> found = dataBase.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNotNull(found);
	}

	@Test
	public void merge() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		dataBase.persist(indexable);

		indexable.setName(name);
		dataBase.merge(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, name);
		Indexable<?> merged = dataBase.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertEquals(indexable.getName(), merged.getName());
	}

	@Test
	public void remove() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		indexable.setName(name);
		dataBase.persist(indexable);
		dataBase.remove(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, name);
		Indexable<?> removed = dataBase.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNull(removed);
	}

	@Test
	public void findClassParametersUnique() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		indexable.setName(name);

		dataBase.persist(indexable);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, name);
		Indexable<?> persisted = dataBase.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNotNull(persisted);

		dataBase.remove(persisted);
		persisted = dataBase.find(indexable.getClass(), parameters, Boolean.TRUE);
		assertNull(persisted);
	}

	@Test
	public void findClassFirstMax() {
		for (int i = 0; i < 100; i++) {
			Token token = new Token();
			token.setId(i);
			dataBase.persist(token);
		}

		int first = 0;
		int max = 10;
		List<Token> tokens = dataBase.find(Token.class, first, max);
		assertEquals(max, tokens.size());

		first = 10;
		max = 50;
		tokens = dataBase.find(Token.class, first, max);
		assertEquals(max, tokens.size());

		first = 90;
		max = 100;
		tokens = dataBase.find(Token.class, first, max);
		assertEquals(10, tokens.size());
	}

	@Test
	public void findClassParametersFirstMax() {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			Token token = new Token();
			token.setId(i);
			token.setStart(start);
			dataBase.persist(token);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.START, start);
		int first = 0;
		int max = 10;
		List<Token> tokens = dataBase.find(Token.class, parameters, first, max);
		assertEquals(max, tokens.size());

		first = 10;
		max = 50;
		tokens = dataBase.find(Token.class, parameters, first, max);
		assertEquals(max, tokens.size());

		first = 90;
		max = 100;
		tokens = dataBase.find(Token.class, parameters, first, max);
		assertEquals(10, tokens.size());
	}

	@Test
	public void performance() throws Exception {
		int iterations = 1000;

		final List<Token> tokens = new ArrayList<Token>();
		for (int i = 0; i < iterations; i++) {
			Token token = new Token();
			token.setId(System.nanoTime());
			tokens.add(token);
			Thread.sleep(1);
		}

		// Persist
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			Iterator<Token> iterator = tokens.iterator();

			@Override
			public void execute() throws Exception {
				dataBase.persist(iterator.next());
			}
		}, "Database persist performance : ", iterations);
		// assertTrue(executionsPerSecond > 1000);

		// Find
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {

			Iterator<Token> iterator = tokens.iterator();
			Map<String, Object> parameters = new HashMap<String, Object>();

			@Override
			public void execute() throws Exception {
				Token token = iterator.next();
				parameters.put(IConstants.ID, token.getId());
				dataBase.find(token.getClass(), parameters, Boolean.TRUE);
			}
		}, "Database find performance : ", iterations);
		// assertTrue(executionsPerSecond > 1000);
		
		// Merge
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			Iterator<Token> iterator = tokens.iterator();

			@Override
			public void execute() throws Exception {
				Token token = iterator.next();
				token.setStart(System.nanoTime());
				dataBase.merge(token);
			}
		}, "Database merge performance : ", iterations);
		// assertTrue(executionsPerSecond > 10);
		
		// Remove
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			Iterator<Token> iterator = tokens.iterator();

			@Override
			public void execute() throws Exception {
				dataBase.remove(iterator.next());
			}
		}, "Database remove performance : ", iterations);
		// assertTrue(executionsPerSecond > 10);
	}

	@Test
	public void threading() throws Exception {
		final int iterations = 1000;
		int threadCount = 3;
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					long start = 0;
					for (int i = 0; i < iterations; i++) {
						double random = Math.random();
						if (random < 0.25) {
							// Persist
							Token token = new Token();
							start = System.nanoTime();
							token.setStart(start);
							log("Persisting : ", i, token);
							dataBase.persist(token);
						} else if (random >= 0.25 && random < 0.5) {
							// Find
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(IConstants.START, start);
							log("Finding : ", i, null);
							dataBase.find(Token.class, parameters, Boolean.FALSE);
						} else if (random >= 5 && random < 0.75) {
							// Merge
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(IConstants.START, start);
							Token token = dataBase.find(Token.class, parameters, Boolean.FALSE);
							if (token != null) {
								token.setStart(System.nanoTime());
							}
							dataBase.merge(token);
						} else {
							// Remove
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(IConstants.START, start);
							Token token = dataBase.find(Token.class, parameters, Boolean.FALSE);
							if (token != null) {
								log("Removing : ", i, token);
								dataBase.remove(token);
							}
						}
					}
				}

				protected void log(String action, int iteration, Token token) {
					if (iteration % 100 == 0) {
						logger.debug(Logging.getString("Action : ", action, ", ", iteration, ", ", token));
					}
				}
			}, this.getClass().getSimpleName() + "." + i);
			threads.add(thread);
			thread.start();
		}
		waitForThreads(threads);
		assertTrue(Boolean.TRUE);
	}

	public static void main(String[] args) throws Exception {
		DataBaseOdbTest.beforeClass();

		DataBaseOdbTest dataBaseOdbTest = new DataBaseOdbTest();
		dataBaseOdbTest.before();
		dataBaseOdbTest.threading();

		dataBaseOdbTest.after();
		DataBaseOdbTest.afterClass();
	}

}
