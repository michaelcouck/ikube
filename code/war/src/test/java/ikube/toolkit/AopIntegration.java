package ikube.toolkit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.Base;
import ikube.index.handler.IStrategy;
import ikube.index.handler.filesystem.IndexableFilesystemHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;

import java.io.File;
import java.util.Arrays;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test is to see if the intercepter is in fact intercepting the classes that it should. Difficult to automate this test of course so
 * it is a manual test. We could of course write an intercepter for the intercepter and verify that the intercepter is called by Spring,
 * personally I think that is a little over kill.
 * 
 * @author Michael Couck
 * @since 30.04.2011
 * @version 01.00
 */
public class AopIntegration extends Base {

	@Cascading
	private Document document;

	@Before
	public void before() {
		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void intercept() throws Exception {
		IndexContext indexContext = ApplicationContextManager.getBean("dropboxIndex");
		IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean("dropboxIndexable");
		IndexableFilesystemHandler indexableHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);

		indexContext.setIndexWriters(mock(IndexWriter.class));
		IStrategy strategy = mock(IStrategy.class);
		when(strategy.preProcess(any(IndexContext.class), any(IndexableFileSystem.class), any(File.class))).thenReturn(Boolean.TRUE);
		indexableFileSystem.setStrategies(Arrays.asList(strategy));

		File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "default.results.xml");
		indexableHandler.handleFile(indexContext, indexableFileSystem, file);

		verify(strategy, atLeastOnce()).preProcess(any(IndexContext.class), any(IndexableFileSystem.class), any(File.class));
	}
}