package ikube.integration.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import ikube.IConstants;
import ikube.action.Prune;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.integration.AbstractIntegration;
import ikube.model.Action;
import ikube.toolkit.ApplicationContextManager;

import java.sql.Timestamp;
import java.util.List;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.09.11
 * @version 01.00
 */
public class PruneIntegration extends AbstractIntegration {

	private Prune prune;
	private IDataBase dataBase;

	@Before
	public void before() throws Exception {
		try {
			// MappingTool.main(new String[] { MappingTool.ACTION_BUILD_SCHEMA });
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		prune = new Prune();
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		Deencapsulation.setField(prune, dataBase);
		Deencapsulation.setField(prune, mock(IClusterManager.class));
		delete(dataBase, Action.class);
	}

	@Test
	public void execute() throws Exception {
		int startIndex = 0;
		int maxResults = 10;
		List<Action> actions = dataBase.find(Action.class, startIndex, maxResults);
		assertEquals("There should be no actions in the database : ", 0, actions.size());

		persistAction();

		actions = dataBase.find(Action.class, startIndex, maxResults);
		assertEquals("There should be one action in the database : ", 1, actions.size());

		boolean result = prune.execute(realIndexContext);
		assertTrue(result);
		actions = dataBase.find(Action.class, startIndex, maxResults);
		assertEquals("There should be one action in the database : ", 1, actions.size());

		for (int i = 0; i < IConstants.MAX_ACTIONS + 100; i++) {
			persistAction();
		}

		actions = dataBase.find(Action.class, 0, Integer.MAX_VALUE);
		assertTrue("There should be a lot of actions in the database : ", actions.size() > IConstants.MAX_ACTIONS);

		result = prune.execute(realIndexContext);
		assertTrue(result);
		actions = dataBase.find(Action.class, 0, Integer.MAX_VALUE);
		assertTrue("There should be less actions in the database than the maximum : ", IConstants.MAX_ACTIONS >= actions.size());
	}

	private void persistAction() {
		Action action = new Action();
		action.setActionName("actionName");
		action.setDuration(System.currentTimeMillis());
		action.setEndTime(new Timestamp(System.currentTimeMillis()));
		action.setIndexableName("indexableName");
		action.setIndexName("indexName");
		action.setStartTime(new Timestamp(System.currentTimeMillis()));
		action.setResult(Boolean.TRUE);

		dataBase.persist(action);
	}

}
