package ikube.index.handler.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.IndexReaderMock;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeltaIndexableFilesystemStrategyTest extends ATest {

	@MockClass(realClass = Collections.class)
	public static class CollectionsMock {
		@Mock
		public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
			return 1;
		}
	}

	private IndexableFileSystem indexableFileSystem;
	private DeltaIndexableFilesystemStrategy deltaStrategy;

	public DeltaIndexableFilesystemStrategyTest() {
		super(DeltaIndexableFilesystemStrategyTest.class);
	}

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() {
		deltaStrategy = new DeltaIndexableFilesystemStrategy(null);

		indexableFileSystem = mock(IndexableFileSystem.class);

		Indexable indexable = (Indexable<?>) indexContext;
		when(indexableFileSystem.getParent()).thenReturn(indexable);
		when(indexableFileSystem.getPathFieldName()).thenReturn("path");
		when(indexableFileSystem.getNameFieldName()).thenReturn("name");
		when(indexableFileSystem.getLengthFieldName()).thenReturn("length");
		when(indexableFileSystem.getLastModifiedFieldName()).thenReturn("last-modified");

		Mockit.setUpMocks(IndexReaderMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(IndexReaderMock.class);
	}

	@Test
	public void aroundProcess() throws Exception {
		File file = Mockito.mock(File.class);
		when(file.getAbsolutePath()).thenReturn(Long.toString(RandomUtils.nextLong()));
		when(file.length()).thenReturn(RandomUtils.nextLong());
		when(file.lastModified()).thenReturn(RandomUtils.nextLong());

		when(indexContext.getHashes()).thenReturn(new ArrayList<Long>());

		boolean mustProcess = deltaStrategy.aroundProcess(indexContext, indexableFileSystem, file);
		assertTrue(mustProcess);

		indexContext.getHashes().add(HashUtilities.hash(file.getAbsolutePath(), file.length(), file.lastModified()));
		indexContext.getHashes().add(HashUtilities.hash(file.getAbsolutePath(), file.length(), Integer.MIN_VALUE));
		indexContext.getHashes().add(HashUtilities.hash(file.getAbsolutePath(), file.length(), Integer.MAX_VALUE));
		Collections.sort(indexContext.getHashes());

		mustProcess = deltaStrategy.aroundProcess(indexContext, indexableFileSystem, file);
		assertFalse(mustProcess);

		assertEquals(2, indexContext.getHashes().size());
	}

	@Test
	public void aroundProcessPerformance() {
		int iterations = 1000;
		final File file = mock(File.class);
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				deltaStrategy.aroundProcess(indexContext, indexableFileSystem, file);
			}
		}, "Delta strategy ", iterations, Boolean.TRUE);
		assertTrue(perSecond > 100);
	}

}