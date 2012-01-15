package ikube.integration.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import static org.junit.Assert.assertTrue;
import ikube.action.Reset;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.integration.AbstractIntegration;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ResetIntegration extends AbstractIntegration {

	private Reset reset;
	private IDataBase dataBase;

	@Before
	public void before() {
		reset = new Reset();
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		Deencapsulation.setField(reset, dataBase);
		Deencapsulation.setField(reset, mock(IClusterManager.class));
		delete(dataBase, Url.class);
	}

	@Test
	public void execute() throws Exception {
		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be no urls in the database : ", 0, urls.size());
		Url url = new Url();

		url.setContentType("");
		url.setHash(System.nanoTime());
		url.setIndexed(Boolean.TRUE);
		url.setName(realIndexContext.getName());
		url.setParsedContent("");
		url.setRawContent(null);
		url.setTitle("");
		url.setUrl("");
		url.setUrlId(System.nanoTime());

		dataBase.persist(url);

		urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be one url in the database : ", 1, urls.size());

		boolean result = reset.execute(realIndexContext);
		assertTrue(result);

		urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals("There should be no urls in the database : ", 0, urls.size());
	}

}
