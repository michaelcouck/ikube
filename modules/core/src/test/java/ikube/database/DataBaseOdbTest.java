package ikube.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.Token;
import ikube.toolkit.ApplicationContextManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataBaseOdbTest extends BaseTest {

	private static DataBaseOdb DATABASE;

	@BeforeClass
	public static void beforeClass() {
		DATABASE = ApplicationContextManager.getBean(DataBaseOdb.class);
		delete(DATABASE, Indexable.class, IndexableColumn.class, Token.class);
	}

	@AfterClass
	public static void afterClass() {
		delete(DATABASE, Indexable.class, IndexableColumn.class, Token.class);
	}

	@Test
	public void getIdField() {
		// Class<?>
		Field field = DATABASE.getIdField(IndexableColumn.class, null);
		assertNotNull(field);
		assertEquals("id", field.getName());
	}

	@Test
	public void getIdFieldName() {
		// Class<?>
		String fieldName = DATABASE.getIdFieldName(IndexableColumn.class);
		assertNotNull(fieldName);
		assertEquals("id", fieldName);
	}

	@Test
	public void setIdField() {
		// T, long
		Indexable<IndexableColumn> indexable = new IndexableColumn();
		DATABASE.setIdField(indexable, Long.MAX_VALUE);
		assertEquals((Long) Long.MAX_VALUE, (Long) indexable.getId());
	}

	@Test
	public void persist() {
		Indexable<?> indexable = new IndexableColumn();
		DATABASE.persist(indexable);
		// Check that this indexable has the id set
		logger.info("Id : " + indexable.getId());
		assertTrue(indexable.getId() != 0);
	}

	@Test
	public void findClassId() {
		Indexable<?> indexable = new IndexableColumn();
		DATABASE.persist(indexable);

		logger.info("Id : " + indexable.getId());
		Indexable<?> persisted = DATABASE.find(indexable.getClass(), indexable.getId());
		assertNotNull(persisted);
		assertEquals(indexable.getId(), persisted.getId());
	}

	@Test
	public void merge() {
		String name = "name";
		Indexable<?> indexable = new IndexableColumn();
		DATABASE.persist(indexable);

		indexable.setName(name);
		DATABASE.merge(indexable);

		Indexable<?> merged = DATABASE.find(indexable.getClass(), indexable.getId());
		assertEquals(indexable.getName(), merged.getName());
	}

	@Test
	public void removeClassId() {
		Indexable<?> indexable = new IndexableColumn();
		DATABASE.persist(indexable);
		DATABASE.remove(indexable.getClass(), indexable.getId());
		Indexable<?> removed = DATABASE.find(indexable.getClass(), indexable.getId());
		assertNull(removed);
	}

	@Test
	public void removeObject() {
		Indexable<?> indexable = new IndexableColumn();
		DATABASE.persist(indexable);
		DATABASE.remove(indexable);
		Indexable<?> removed = DATABASE.find(indexable.getClass(), indexable.getId());
		assertNull(removed);
	}

	@Test
	public void findClassParametersUnique() {
		String ip = "ip";
		Token token = new Token();
		token.setIp(ip);
		DATABASE.persist(token);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.IP, ip);

		Token persisted = DATABASE.find(Token.class, parameters, Boolean.TRUE);
		assertNotNull(persisted);

		DATABASE.remove(persisted);
		persisted = DATABASE.find(Token.class, parameters, Boolean.TRUE);
		assertNull(persisted);
	}

	@Test
	public void findClassParametersFirstMax() {
		String ip = "ipAddress";
		for (int i = 0; i < 100; i++) {
			Token token = new Token();
			token.setIp(ip);
			DATABASE.persist(token);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.IP, ip);

		int first = 0;
		int max = 10;
		List<Token> tokens = DATABASE.find(Token.class, parameters, first, max);
		assertEquals(max, tokens.size());

		first = 10;
		max = 50;
		tokens = DATABASE.find(Token.class, parameters, first, max);
		assertEquals(max, tokens.size());

		first = 90;
		max = 100;
		tokens = DATABASE.find(Token.class, parameters, first, max);
		assertEquals(10, tokens.size());
	}

	// @Test
	// public void lockRelease() throws Exception {
	// List<Thread> threads = new ArrayList<Thread>();
	// Thread thread = null;
	// for (int i = 0; i < 3; i++) {
	// thread = new Thread() {
	// int iterations;
	//
	// public void run() {
	// while (iterations++ < 10) {
	// try {
	// Thread.sleep((long) (Math.random() * 10));
	// } catch (Exception e) {
	// logger.error("", e);
	// }
	// logger.info("Locking : " + this.hashCode());
	// Lock lock = DATABASE.lock(Indexable.class);
	// logger.info("Got lock : " + this.hashCode());
	// try {
	// Thread.sleep((long) (Math.random() * 10));
	// } catch (Exception e) {
	// logger.error("", e);
	// }
	// logger.info("Releasing : " + this.hashCode());
	// DATABASE.release(lock);
	// logger.info("Released : " + this.hashCode());
	// }
	// }
	// };
	// thread.start();
	// threads.add(thread);
	// }
	// waitForClients(threads);
	// }

	/**
	 * This method just joins the threads in the group until they are all finished.
	 *
	 * @param clients
	 *            the clients to wait for until they are all finished
	 */
	protected synchronized void waitForClients(List<Thread> clients) {
		while (true) {
			try {
				wait(3000);
				if (!isCrawling(clients)) {
					break;
				}
				// Join any one of the threads that is still alive
				for (Thread client : clients) {
					if (client.isAlive()) {
						client.join(10000);
						break;
					}
				}
			} catch (InterruptedException e) {
				logger.error("Exception waiting for handlers to finish : ", e);
			}
		}
	}

	/**
	 * This method checks to see if there are any threads in the group that are still alive.
	 *
	 * @param clients
	 *            the thread group to check for alive threads
	 * @return whether there were any threads in the group that were alive
	 */
	private synchronized boolean isCrawling(List<Thread> clients) {
		if (clients == null) {
			return Boolean.FALSE;
		}
		boolean isCrawling = Boolean.FALSE;
		for (Thread client : clients) {
			if (client.isAlive()) {
				isCrawling = Boolean.TRUE;
			}
		}
		return isCrawling;
	}

}
