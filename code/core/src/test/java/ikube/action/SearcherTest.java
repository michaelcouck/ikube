package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class SearcherTest extends ATest {

	public SearcherTest() {
		super(SearcherTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class, ClusterManagerMock.class);
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);
		when(CLUSTER_MANAGER.setWorking(anyString(), anyString(), anyBoolean())).thenReturn(System.currentTimeMillis());
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void validate() throws Exception {
		Searcher searcher = new Searcher();
		boolean result = searcher.execute(INDEX_CONTEXT);
		assertTrue(result);
	}
}