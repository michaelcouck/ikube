package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.database.DataBaseOdb;
import ikube.database.IDataBase;
import ikube.model.Batch;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ClusterManager;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ResetTest extends BaseActionTest {

	private Reset reset = new Reset();

	@Test
	public void execute() {
		delete(ApplicationContextManager.getBean(IDataBase.class), Batch.class);
		// Set another server working on a different action
		String actionName = "actionName";
		String anotherServerName = "anotherServer";
		IndexContext otherIndexContext = mock(IndexContext.class);
		when(otherIndexContext.getServerName()).thenReturn(anotherServerName);
		when(otherIndexContext.getIndexName()).thenReturn(this.indexContext.getIndexName());

		ClusterManager.setWorking(otherIndexContext, actionName, Boolean.TRUE);
		boolean done = reset.execute(indexContext);
		assertFalse(done);
		ClusterManager.setWorking(otherIndexContext, actionName, Boolean.FALSE);

		DataBaseOdb dataBaseOdb = ApplicationContextManager.getBean(DataBaseOdb.class);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
		Batch batch = dataBaseOdb.find(Batch.class, parameters, Boolean.TRUE);
		if (batch == null) {
			batch = new Batch();
		}
		batch.setNextRowNumber(1000);
		dataBaseOdb.merge(batch);

		// No other servers are working so it will return sucessfully
		done = reset.execute(indexContext);
		assertTrue(done);

		// Set a working with this index but not working and the batch more than 0
		parameters = new HashMap<String, Object>();
		parameters.put(IConstants.SERVER_NAME, anotherServerName);
		Server server = dataBaseOdb.find(Server.class, parameters, Boolean.TRUE);
		server.setActionName(Reset.class.getName());
		server.setWorking(Boolean.FALSE);
		dataBaseOdb.merge(server);

		done = reset.execute(indexContext);
		assertTrue(done);

		parameters.remove(IConstants.SERVER_NAME);
		batch = dataBaseOdb.find(Batch.class, parameters, Boolean.TRUE);
		assertTrue(batch.getNextRowNumber() == 0);
	}

}
