package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.database.DataBaseOdb;
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
		DataBaseOdb dataBaseOdb = ApplicationContextManager.getBean(DataBaseOdb.class);
		delete(dataBaseOdb, Server.class, Batch.class);

		// Do a reset on this batch, no servers running and the batch number should be 0 afterwards
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
		Batch batch = dataBaseOdb.find(Batch.class, parameters, Boolean.TRUE);
		if (batch == null) {
			batch = new Batch();
			batch.setIndexName(indexContext.getIndexName());
			dataBaseOdb.persist(batch);
		}
		batch.setNextRowNumber(1000);
		dataBaseOdb.merge(batch);

		// No other servers are working so it will return sucessfully
		boolean done = reset.execute(indexContext);
		assertTrue(done);
		batch = dataBaseOdb.find(Batch.class, parameters, Boolean.TRUE);
		assertEquals(0, batch.getNextRowNumber());

		// Set another server working on a different action but on this index. The
		// result of the reset should be false as we don't want to reset anything while
		// there are servers working on an action other than this action and this index
		String anotherActionName = "anotherActionName";
		String anotherServerName = "anotherServer";
		IndexContext otherIndexContext = mock(IndexContext.class);
		when(otherIndexContext.getServerName()).thenReturn(anotherServerName);
		when(otherIndexContext.getIndexName()).thenReturn(this.indexContext.getIndexName());

		ClusterManager.setWorking(otherIndexContext, anotherActionName, Boolean.TRUE);
		done = reset.execute(indexContext);
		assertFalse(done);
		ClusterManager.setWorking(otherIndexContext, anotherActionName, Boolean.FALSE);

		// Set a working with this index but not working and the batch more than 0
		parameters = new HashMap<String, Object>();
		parameters.put(IConstants.SERVER_NAME, anotherServerName);
		Server server = dataBaseOdb.find(Server.class, parameters, Boolean.TRUE);
		server.setActionName(Reset.class.getName());
		server.setWorking(Boolean.FALSE);
		dataBaseOdb.merge(server);

		done = reset.execute(indexContext);
		assertTrue(done);

		int nextBatchNumber = ClusterManager.getNextBatchNumber(otherIndexContext);
		assertEquals(0, nextBatchNumber);
	}

}
