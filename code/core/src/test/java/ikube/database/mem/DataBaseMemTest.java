package ikube.database.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.ATest;
import ikube.database.IDataBase;
import ikube.model.Url;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DataBaseMemTest extends ATest {

	private IDataBase dataBase;

	public DataBaseMemTest() {
		super(DataBaseMemTest.class);
	}

	@Before
	public void before() {
		dataBase = new DataBaseMem();
		delete(dataBase, Url.class);
	}

	@After
	public void after() {
		delete(dataBase, Url.class);
	}

	@Test
	public void persist() throws Exception {
		// T
		Url url = new Url();
		url.setId(System.nanoTime());
		dataBase.persist(url);
		url = dataBase.find(Url.class, url.getId());
		assertNotNull(url);
		dataBase.remove(url);
		url = dataBase.find(Url.class, url.getId());
		assertNull(url);
	}

	@Test
	public void findClassIntInt() throws Exception {
		// Class<T>, int, int
		int total = 100;
		for (int i = 0; i < total; i++) {
			Url url = new Url();
			url.setId(new Long(i));
			dataBase.persist(url);
		}
		int start = 0;
		int end = 9;
		List<Url> urls = dataBase.find(Url.class, start, end);
		assertEquals(end, urls.size());

		start = 11;
		end = 8;
		urls = dataBase.find(Url.class, start, end);
		assertEquals(end, urls.size());

		start = 95;
		end = 10;
		urls = dataBase.find(Url.class, start, end);
		assertEquals(total - start, urls.size());
	}

	@Test
	public void findClassLong() throws Exception {
		// Class<T>, Long
		Url url = new Url();
		url.setId(new Long(1));
		dataBase.persist(url);
		url = dataBase.find(Url.class, url.getId());
		assertNotNull(url);
	}

	@Test
	public void removeClassLong() throws Exception {
		// Class<T>, Long
		Url url = new Url();
		url.setId(new Long(1));
		dataBase.persist(url);
		url = dataBase.find(Url.class, url.getId());
		assertNotNull(url);
		dataBase.remove(Url.class, url.getId());
		url = dataBase.find(Url.class, url.getId());
		assertNull(url);
	}

	@Test
	public void remove() throws Exception {
		// T
		Url url = new Url();
		url.setId(new Long(1));
		dataBase.persist(url);
		url = dataBase.find(Url.class, url.getId());
		assertNotNull(url);
		dataBase.remove(url);
		url = dataBase.find(Url.class, url.getId());
		assertNull(url);
	}

}