package ikube.index.handler.filesystem;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.ATest;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemLogHandlerTest extends ATest {

	private IndexableFilesystemLogHandler indexableFilesystemLogHandler;

	public IndexableFilesystemLogHandlerTest() {
		super(IndexableFilesystemLogHandlerTest.class);
	}

	@Before
	public void before() {
		indexableFilesystemLogHandler = new IndexableFilesystemLogHandler();
	}

	@Test
	public void handle() throws Exception {
		IndexableFileSystemLog indexableFileSystemLog = new IndexableFileSystemLog();
		File logDirectory = FileUtilities.findFileRecursively(new File("."), "ikube\\.1\\.log");
		indexableFileSystemLog.setPath(logDirectory.getParentFile().getAbsolutePath());
		indexableFileSystemLog.setLineFieldName("lineNumber");
		indexableFileSystemLog.setContentFieldName("lineContents");
		List<Future<?>> futures = indexableFilesystemLogHandler.handle(indexContext, indexableFileSystemLog);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		verify(indexWriter, atLeastOnce()).addDocument(any(Document.class));
	}

}