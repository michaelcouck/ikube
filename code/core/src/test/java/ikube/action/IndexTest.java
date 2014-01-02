package ikube.action;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.action.index.handler.database.IndexableTableHandler;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableTable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexTest extends AbstractTest {

	private Index index;

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() throws Exception {
		index = new Index();
		indexableTable = new IndexableTable();
		indexableTable.setName("indexableName");
		List<Indexable<?>> indexables = new ArrayList<Indexable<?>>(Arrays.asList(indexableTable));

		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class);
		ForkJoinTask forkJoinTask = new RecursiveAction() {
			@Override
			protected void compute() {
				return;
			}
		};

		when(clusterManager.startWorking(anyString(), anyString(), anyString())).thenReturn(action);
		when(clusterManager.getServer()).thenReturn(server);
		when(action.getStartTime()).thenReturn(new Timestamp(System.currentTimeMillis()));
		when(action.getActionName()).thenReturn(Index.class.getSimpleName());
		when(action.getIndexName()).thenReturn("indexName");
		when(action.getIndexableName()).thenReturn(indexableTable.getName());
		when(indexContext.getName()).thenReturn("indexName");
		when(indexContext.getChildren()).thenReturn(indexables);
		when(ApplicationContextManagerMock.HANDLER.handleIndexableForked(any(IndexContext.class), any(IndexableTable.class))).thenReturn(forkJoinTask);

		Logger logger = Mockito.mock(Logger.class);
		Deencapsulation.setField(index, logger);
		Deencapsulation.setField(index, dataBase);
		Deencapsulation.setField(index, clusterManager);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		Mockit.tearDownMocks(IndexManagerMock.class, ApplicationContextManagerMock.class);
	}

	@Test
	public void preExecute() throws Exception {
		// TODO Re-implement this in the delta strategy?
	}

	@Test
	public void execute() throws Exception {
		boolean result = index.execute(indexContext);
		logger.info("Result from index action : " + result);
		assertTrue("The index must execute properly : ", result);
	}

	@Test
	public void postExecute() {
		// TODO Re-implement this in the delta strategy?
	}

	@Test
	public void getAction() {
		Action action = index.getAction(server, indexContext, indexableTable.getName());
		assertNotNull(action);
	}

	@Test
	public void getHandler() {
		IndexableTable indexableTable = new IndexableTable();
		Object handler = Deencapsulation.invoke(new Index(), "getHandler", indexableTable);
		assertTrue("The handler for the Arabic data should be the wiki handler : ",
				handler.getClass().getName().contains(IndexableTableHandler.class.getSimpleName()));
	}

}
