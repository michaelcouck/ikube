package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemWikiHandlerTest extends ATest {

	private IndexableFilesystemWikiHandler indexableFilesystemWikiHandler;

	public IndexableFilesystemWikiHandlerTest() {
		super(IndexableFilesystemWikiHandlerTest.class);
	}

	@Before
	public void before() {
		indexableFilesystemWikiHandler = new IndexableFilesystemWikiHandler();
	}

	@Test
	public void handle() throws Exception {
		ThreadUtilities.destroy();
		IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
		File file = FileUtilities.findFileRecursively(new File("."), "bzip2.bz2");
		indexableFileSystem.setPath(file.getAbsolutePath());
		List<Future<?>> futures = indexableFilesystemWikiHandler.handle(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		
	}

}
