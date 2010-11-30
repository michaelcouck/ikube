package ikube.index.handler.filesystem;

import ikube.BaseTest;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFileSystemHandlerTest extends BaseTest {

	@Test
	public void handle() throws Exception {
		indexContext.setIndexWriter(indexWriter);
		IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean(IndexableFileSystem.class);
		IndexableFilesystemHandler indexableFileSystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);
		indexableFileSystemHandler.handle(indexContext, indexableFileSystem);

		// TODO - verify that there are some records indexed. This must wait until
		// the clustering logic is decided upon for the url handler as this will have an impact on
		// the way the data is shared in the cluster
	}

}
