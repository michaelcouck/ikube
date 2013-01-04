package ikube.toolkit;

import ikube.Base;
import ikube.index.handler.IStrategy;
import ikube.index.handler.filesystem.IndexableFilesystemHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;

import java.io.File;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
		IndexContext indexContext = ApplicationContextManager.getBean("desktop");
		IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		IndexableFilesystemHandler indexableHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);

		indexContext.setIndexWriters(Mockito.mock(IndexWriter.class));
		IStrategy strategy = Mockito.mock(IStrategy.class);
		Mockito.when(strategy.preProcess(Mockito.any(IndexContext.class), Mockito.any(IndexableFileSystem.class), Mockito.any(File.class)))
				.thenReturn(Boolean.TRUE);
		indexableFileSystem.getStrategies().add(strategy);

		File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "default.results.xml");
		indexableHandler.handleFile(indexContext, indexableFileSystem, file);

		Mockito.verify(strategy, Mockito.atLeastOnce()).preProcess(Mockito.any(IndexContext.class), Mockito.any(IndexableFileSystem.class),
				Mockito.any(File.class));
	}
}