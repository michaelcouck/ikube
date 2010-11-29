package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.database.IDataBase;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ResetTest extends BaseActionTest {

	private Reset reset = new Reset();

	@Test
	public void execute() throws Exception {
		indexContext.setIdNumber(1);
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		Url url = new Url();
		url.setName("internet");
		url.setUrl("dummy");
		dataBase.persist(url);

		List<Url> urls = dataBase.find(Url.class, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertTrue(urls.size() == 1);

		reset.execute(indexContext);

		urls = dataBase.find(Url.class, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertTrue(urls.size() == 0);
	}

}
