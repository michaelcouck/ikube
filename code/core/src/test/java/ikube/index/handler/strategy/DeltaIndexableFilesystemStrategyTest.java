package ikube.index.handler.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.search.Search;
import ikube.search.SearchMulti;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mockit;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeltaIndexableFilesystemStrategyTest extends ATest {

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

		new MockUp<Search>() {
			@Mock
			public ArrayList<HashMap<String, String>> execute() {
				return new ArrayList<HashMap<String, String>>();
			}
		};
		new MockUp<SearchMulti>() {
			@Mock
			public Query getQuery() throws ParseException {
				return Mockito.mock(Query.class);
			}
		};
		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void preProcess() throws CorruptIndexException, IOException {
		boolean mustProcess = deltaStrategy.preProcess(indexContext, indexableFileSystem, file);
		assertFalse(mustProcess);

		new MockUp<Search>() {
			@Mock
			public ArrayList<HashMap<String, String>> execute() {
				return new ArrayList<HashMap<String, String>>(Arrays.asList(new HashMap<String, String>()));
			}
		};
		mustProcess = deltaStrategy.preProcess(indexContext, indexableFileSystem, file);
		assertTrue(mustProcess);

		Mockito.verify(indexWriter, Mockito.atLeast(1)).deleteDocuments(Mockito.any(Query.class));
	}

	@Test
	public void postProcess() {
		boolean mustProcess = deltaStrategy.postProcess(indexContext, indexableFileSystem, file);
		assertTrue(mustProcess);
	}

	@Test
	public void performance() {
		int iterations = 1000;
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				deltaStrategy.preProcess(indexContext, indexableFileSystem, file);
			}
		}, "Delta strategy ", iterations, Boolean.TRUE);
		assertTrue(perSecond > 100);
	}

}