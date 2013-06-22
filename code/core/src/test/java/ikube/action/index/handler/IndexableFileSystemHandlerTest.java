package ikube.action.index.handler;

import java.util.List;
import java.util.concurrent.Future;

import ikube.AbstractTest;
import ikube.action.index.handler.filesystem.IndexableFileSystemHandler;
import ikube.model.IndexableFileSystem;

import org.junit.Test;
import org.mockito.Mockito;

public class IndexableFileSystemHandlerTest extends AbstractTest {

	@Test
	public void handleIndexable() throws Exception {
		IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
		indexableFileSystem.setPath(".");
		IndexableFileSystemHandler indexableFileSystemHandler = new IndexableFileSystemHandler();
		indexableFileSystem.setThreads(3);
		Mockito.when(indexContext.getThrottle()).thenReturn(1l);
		List<Future<?>> futures = indexableFileSystemHandler.handleIndexable(indexContext, indexableFileSystem);
		logger.info("Done : " + futures);
	}

}
