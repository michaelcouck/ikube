package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

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
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);
		when(CLUSTER_MANAGER.setWorking(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(System.currentTimeMillis());
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		Mockit.tearDownMocks(IndexManager.class, ApplicationContextManager.class);
	}

	@Test
	public void execute() throws Exception {
		boolean result = new Index().execute(INDEX_CONTEXT);
		assertTrue(result);
	}

}
