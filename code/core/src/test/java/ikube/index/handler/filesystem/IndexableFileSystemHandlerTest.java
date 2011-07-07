package ikube.index.handler.filesystem;

import ikube.BaseTest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends BaseTest {

	private IndexableFilesystemHandler indexableFileSystemHandler;

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		indexableFileSystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);
	}

	@Test
	public void handle() throws Exception {
		IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean("filesystem");
		List<Thread> threads = indexableFileSystemHandler.handle(INDEX_CONTEXT, indexableFileSystem);
		ThreadUtilities.waitForThreads(threads);
		// TODO Verify that there are some files indexed
	}

	@Test
	public void getBatch() {
		// IDataBase, IndexableFileSystem
		// TODO Implement me
	}

	@Test
	public void persistFilesRecurse() {
		// IDataBase, IndexContext<?>, IndexableFileSystem, File, Pattern, Set<File>
		// TODO Implement me
	}

	@Test
	public void persistFilesBatch() {
		// IDataBase, IndexableFileSystem, Set<File>
		// TODO Implement me
	}

	@Test
	public void handleFile() {
		// IndexContext<?>, IndexableFileSystem, File
		// TODO Implement me
	}

	@Test
	public void getPattern() {
		// String
		// TODO Implement me
	}

	@Test
	public void isExcluded() {
		// File, Pattern
	}

}