package ikube.index.handler.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.service.ISearcherService;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeltaIndexableFilesystemStrategyTest extends ATest {

	private File file;
	private ISearcherService searcherService;
	private IndexableFileSystem indexableFileSystem;
	private DeltaIndexableFilesystemStrategy deltaStrategy;

	public DeltaIndexableFilesystemStrategyTest() {
		super(DeltaIndexableFilesystemStrategyTest.class);
	}

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() {
		deltaStrategy = new DeltaIndexableFilesystemStrategy(null);

		file = mock(File.class);
		indexableFileSystem = mock(IndexableFileSystem.class);
		searcherService = mock(ISearcherService.class);

		Indexable indexable = (Indexable<?>) indexContext;
		when(indexableFileSystem.getParent()).thenReturn(indexable);
		when(indexableFileSystem.getLengthFieldName()).thenReturn("length");
		when(indexableFileSystem.getLastModifiedFieldName()).thenReturn("last-modified");
		when(indexableFileSystem.getPathFieldName()).thenReturn("path-field-name");
		when(indexableFileSystem.getNameFieldName()).thenReturn("name-field-name");

		when(file.getName()).thenReturn("file-name");
		when(file.getAbsolutePath()).thenReturn("absolute-path");

		Deencapsulation.setField(deltaStrategy, searcherService);
	}

	@Test
	public void preProcess() throws CorruptIndexException, IOException {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();

		when(file.length()).thenReturn(1000l);
		when(file.lastModified()).thenReturn(1000l);
		when(searcherService.searchMulti(anyString(), any(String[].class), any(String[].class), anyBoolean(), anyInt(), anyInt()))
				.thenReturn(results);

		boolean mustProcess = deltaStrategy.preProcess(indexContext, indexableFileSystem, file);
		assertTrue(mustProcess);

		HashMap<String, String> result = new HashMap<String, String>();
		result.put(indexableFileSystem.getLengthFieldName(), "1000");
		result.put(indexableFileSystem.getLastModifiedFieldName(), "1000");
		result.put(indexableFileSystem.getNameFieldName(), "file-name");
		result.put(indexableFileSystem.getPathFieldName(), "absolute-path");
		result.put(IConstants.FILE_ID, HashUtilities.hash(file.getAbsolutePath()).toString());

		results.add(result);
		results.add(result);

		mustProcess = deltaStrategy.preProcess(indexContext, indexableFileSystem, file);
		assertFalse(mustProcess);

		when(file.length()).thenReturn(10000l);
		when(file.lastModified()).thenReturn(10000l);
		mustProcess = deltaStrategy.preProcess(indexContext, indexableFileSystem, file);
		assertTrue(mustProcess);

		Mockito.verify(indexWriter, Mockito.atLeast(1)).deleteDocuments(Mockito.any(Term.class));
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