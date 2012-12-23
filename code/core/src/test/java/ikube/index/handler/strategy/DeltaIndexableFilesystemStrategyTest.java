package ikube.index.handler.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.search.Search;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeltaIndexableFilesystemStrategyTest extends ATest {

	@MockClass(realClass = Search.class)
	public static class SearchMock {
		static ArrayList<HashMap<String, String>> RESULTS;

		@Mock
		public ArrayList<HashMap<String, String>> execute() {
			return RESULTS;
		}
	}

	private File file;
	private IndexableFileSystem indexableFileSystem;
	private DeltaIndexableFilesystemStrategy deltaStrategy;

	public DeltaIndexableFilesystemStrategyTest() {
		super(DeltaIndexableFilesystemStrategyTest.class);
	}

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() {
		deltaStrategy = new DeltaIndexableFilesystemStrategy(null);

		file = Mockito.mock(File.class);
		indexableFileSystem = Mockito.mock(IndexableFileSystem.class);
		Indexable indexable = (Indexable<?>) indexContext;
		Mockito.when(indexableFileSystem.getParent()).thenReturn(indexable);
		Mockito.when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);

		Mockit.setUpMock(SearchMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(SearchMock.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void preProcess() {
		SearchMock.RESULTS = new ArrayList<HashMap<String, String>>();
		boolean mustProcess = deltaStrategy.preProcess(indexableFileSystem, file);
		assertFalse(mustProcess);

		SearchMock.RESULTS = new ArrayList<HashMap<String, String>>(Arrays.asList(new HashMap<String, String>()));
		mustProcess = deltaStrategy.preProcess(indexableFileSystem, file);
		assertTrue(mustProcess);
	}

	@Test
	public void postProcess() {
		boolean mustProcess = deltaStrategy.postProcess(indexableFileSystem, file);
		assertTrue(mustProcess);
	}

	@Test
	public void performance() {
		int iterations = 1000;
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				deltaStrategy.preProcess(indexableFileSystem, file);
			}
		}, "Delta strategy ", iterations, Boolean.TRUE);
		assertTrue(perSecond > 100);
	}

}