package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the general functionality of the file system handler. There are no specific checks on the data that is indexed as the sub
 * components are tested separately and the integration tests verify that the data is collected.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends ATest {

	/** Class under test. */
	private IndexableFilesystemHandler indexableFileSystemHandler;

	public IndexableFileSystemHandlerTest() {
		super(IndexableFileSystemHandlerTest.class);
	}

	@Before
	public void before() {
		indexableFileSystemHandler = new IndexableFilesystemHandler();
		indexableFileSystemHandler.setThreads(1);
	}

	@Test
	public void handle() throws Exception {
		// ThreadUtilities.destroy();
		IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
		indexableFileSystem.setPath("./");
		List<Future<?>> futures = indexableFileSystemHandler.handle(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		assertTrue("There should be some nulls in the futures as the executer is whut down : ", futures.size() > 0);
	}

}