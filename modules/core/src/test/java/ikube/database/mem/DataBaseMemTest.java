package ikube.database.mem;

import static org.junit.Assert.*;

import java.util.List;

import ikube.BaseTest;
import ikube.database.IDataBase;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

public class DataBaseMemTest extends BaseTest {

	private IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);

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
			url.setId(i);
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
	}

	@Test
	public void removeClassLong() throws Exception {
		// Class<T>, Long
	}

	@Test
	public void remove() throws Exception {
		// T
	}

}