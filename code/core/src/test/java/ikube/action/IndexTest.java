package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.index.handler.IHandler;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableTable;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexTest extends ATest {

	private Index index = mock(Index.class);
	@SuppressWarnings("rawtypes")
	private IHandler handler = mock(IHandler.class);

	public IndexTest() {
		super(IndexTest.class);
	}

	@Before
	@SuppressWarnings({ "unchecked" })
	public void before() throws Exception {
		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class);
		when(clusterManager.startWorking(anyString(), anyString(), anyString())).thenReturn(action);
		when(clusterManager.getServer()).thenReturn(server);
		when(action.getStartTime()).thenReturn(new Timestamp(System.currentTimeMillis()));
		when(index.getAction(any(Server.class), anyLong())).thenReturn(action);
		when(index.execute(any(IndexContext.class))).thenCallRealMethod();
		when(index.executeInternal(any(IndexContext.class))).thenCallRealMethod();
		when(index.executeIndexables(any(IndexContext.class), any(Iterator.class))).thenCallRealMethod();
		when(index.getHandler(any(Indexable.class))).thenReturn(handler);
		
		Deencapsulation.setField(index, logger);
		Deencapsulation.setField(index, clusterManager);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		Mockit.tearDownMocks(IndexManager.class, ApplicationContextManager.class);
		// ThreadUtilities.destroy();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {
		Deencapsulation.setField(index, clusterManager);
		boolean result = index.execute(indexContext);
		assertTrue(result);
		Mockito.verify(handler, Mockito.atLeastOnce()).handle(any(IndexContext.class), any(Indexable.class));
	}

	@Test
	public void executeSuccess() throws Exception {
		indexableTable = new IndexableTable();
		List<Indexable<?>> indexables = new ArrayList<Indexable<?>>(Arrays.asList(indexableTable));
		Mockito.when(indexContext.getIndexables()).thenReturn(indexables);
		boolean result = index.execute(indexContext);
		logger.info("Result from index action : " + result);
		assertTrue("The index must execute properly : ", result);
	}

	@Test
	public void getHandler() {
		IndexableTable indexableTable = new IndexableTable();
		Object handler = Deencapsulation.invoke(new Index(), "getHandler", indexableTable);
		assertTrue("The handler for the Arabic data should be the wiki handler : ",
				handler.getClass().getName().contains(IndexableTableHandler.class.getSimpleName()));
	}

}
