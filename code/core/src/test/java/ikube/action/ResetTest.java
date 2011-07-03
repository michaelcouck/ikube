package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.database.IDataBase;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ResetTest extends BaseTest {

	private final Reset reset = new Reset();
	private IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);

	public ResetTest() {
		super(ResetTest.class);
	}

	@Before
	public void before() {
		delete(dataBase, Url.class);
	}

	@Test
	public void execute() {
		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be no urls in the database : ", 0, urls.size());
		Url url = new Url();

		url.setContentType("");
		url.setHash(System.nanoTime());
		url.setIndexed(Boolean.TRUE);
		url.setName(INDEX_CONTEXT.getName());
		url.setParsedContent("");
		url.setRawContent(null);
		url.setTitle("");
		url.setUrl("");
		url.setUrlId(System.nanoTime());

		dataBase.persist(url);

		urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be one url in the database : ", 1, urls.size());

		boolean result = reset.execute(INDEX_CONTEXT);
		assertTrue(result);

		urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be no urls in the database : ", 0, urls.size());
	}

}
