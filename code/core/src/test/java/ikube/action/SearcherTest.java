package ikube.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.mock.ServiceLocatorMock;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
/** TODO Re-do this test. */
@Ignore
public class SearcherTest extends ATest {

	public SearcherTest() {
		super(SearcherTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(IndexManagerMock.class, ApplicationContextManagerMock.class, ClusterManagerMock.class, ServiceLocatorMock.class);
		when(index.getIndexWriter()).thenReturn(indexWriter);
		when(clusterManager.startWorking(anyString(), anyString(), anyString())).thenReturn(action);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void validate() throws Exception {
		Searcher searcher = new Searcher();
		searcher.setEnd(10);
		searcher.setFragment(Boolean.TRUE);
		searcher.setIterations(10);
		searcher.setResultsSizeMinimum(0);
		searcher.setRuleExpression("");
		searcher.setRules(null);
		searcher.setSearchString("Hello World!");
		searcher.setStart(0);
		boolean result = searcher.execute(indexContext);
		assertTrue(result);
	}
}