package ikube.integration.action;

import static org.junit.Assert.*;

import ikube.action.Index;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.handler.filesystem.IndexableFilesystemWikiHandler;
import ikube.integration.AbstractIntegration;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 14.01.12
 * @version 01.00
 */
public class IndexIntegration extends AbstractIntegration {

	private Index index;
	private IDataBase dataBase;
	private IClusterManager clusterManager;

	@Before
	public void before() throws Exception {
		index = ApplicationContextManager.getBean(Index.class);
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Deencapsulation.setField(index, dataBase);
		Deencapsulation.setField(index, clusterManager);
		clusterManager.getServer().getActions().clear();
		delete(dataBase, Action.class);
	}

	@Test
	public void execute() throws Exception {
		IndexContext<?> indexContext = ApplicationContextManager.getBean("dropboxIndex");
		boolean result = index.execute(indexContext);
		logger.info("Result from index action : " + result);
		assertTrue("The index must execute properly : ", result);
		// Stop all the actions
	}
	
	@Test
	public void getHandler() {
		Object wikiHistoryDataArabic = ApplicationContextManager.getBean("wikiHistoryDataArabic");
		Object handler = Deencapsulation.invoke(new Index(), "getHandler", wikiHistoryDataArabic);
		assertEquals(IndexableFilesystemWikiHandler.class, handler.getClass());
	}

}