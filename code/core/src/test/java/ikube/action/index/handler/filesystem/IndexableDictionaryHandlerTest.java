package ikube.action.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.IndexableDictionary;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
public class IndexableDictionaryHandlerTest extends AbstractTest {

	@Before
	public void before() {
		File dictionaryIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(dictionaryIndexDirectory, 1);
	}

	@After
	public void after() {
		File dictionaryIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(dictionaryIndexDirectory, 1);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void handle() throws Exception {
		IndexableDictionary indexableDictionary = mock(IndexableDictionary.class);
		File dictionariesDirectory = FileUtilities.findFileRecursively(new File("."), "dictionaries");
		when(indexableDictionary.getPath()).thenReturn(dictionariesDirectory.getAbsolutePath());
		IndexableDictionaryHandler dictionaryHandler = new IndexableDictionaryHandler();
		indexableDictionary.setThreads(1);
		List<Future<?>> threads = dictionaryHandler.handleIndexable(indexContext, indexableDictionary);
		ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
		File dictionaryIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		boolean indexExists = IndexReader.indexExists(FSDirectory.open(dictionaryIndexDirectory));
		assertTrue("The dictionary index should be created : ", indexExists);
	}

}
