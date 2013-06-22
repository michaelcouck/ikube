package ikube.action.index.handler.filesystem;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.AbstractTest;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import ikube.model.IndexableFileSystemWiki;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableFilesystemWikiHandlerTest extends AbstractTest {

	private IndexableFilesystemWikiHandler indexableFilesystemWikiHandler;

	@Before
	public void before() {
		indexableFilesystemWikiHandler = new IndexableFilesystemWikiHandler();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handle() throws Exception {
		IndexableFileSystemWiki indexableFileSystem = new IndexableFileSystemWiki();
		indexableFileSystem.setLastModifiedFieldName("lastModifiedFieldName");
		indexableFileSystem.setNameFieldName("nameFieldName");
		indexableFileSystem.setLengthFieldName("lengthFieldName");
		indexableFileSystem.setMaxReadLength(Integer.MAX_VALUE);
		indexableFileSystem.setPathFieldName("pathFieldName");
		indexableFileSystem.setContentFieldName("contentFieldName");
		indexableFileSystem.setMaxRevisions(Integer.MAX_VALUE);
		indexableFileSystem.setThreads(3);
		File file = FileUtilities.findFileRecursively(new File("."), "enwiki-revisions.bz2");
		indexableFileSystem.setPath(file.getParentFile().getAbsolutePath());

		ResourceHandler<IndexableFileSystemLog> resourceBaseHandler = Mockito.mock(ResourceHandler.class);
		Deencapsulation.setField(indexableFilesystemWikiHandler, "resourceHandler", resourceBaseHandler);

		List<Future<?>> futures = indexableFilesystemWikiHandler.handleIndexable(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);

		verify(resourceBaseHandler, atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableFileSystemLog.class), any(Document.class),
				any(Object.class));
	}

}