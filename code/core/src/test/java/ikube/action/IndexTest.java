package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.Action;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Timestamp;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexTest extends ATest {

	public IndexTest() {
		super(IndexTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class);
		when(index.getIndexWriter()).thenReturn(indexWriter);
		when(clusterManager.startWorking(anyString(), anyString(), anyString())).thenReturn(System.currentTimeMillis());
		when(clusterManager.getServer()).thenReturn(server);
		Action action = mock(Action.class);
		when(action.getStartTime()).thenReturn(new Timestamp(System.currentTimeMillis()));
		server.getActions().add(action);
		// when(server.getActions().get(0)).thenReturn(action);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		Mockit.tearDownMocks(IndexManager.class, ApplicationContextManager.class);
	}

	@Test
	public void execute() throws Exception {
		Index index = new Index();
		Deencapsulation.setField(index, clusterManager);
		boolean result = index.execute(indexContext);
		assertTrue(result);
	}

}
