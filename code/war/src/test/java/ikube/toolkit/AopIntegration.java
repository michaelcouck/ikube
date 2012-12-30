package ikube.toolkit;

import ikube.Base;
import ikube.index.handler.IStrategy;
import ikube.index.handler.filesystem.IndexableFilesystemHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
@Ignore
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
		indexContext.setIndexWriters(Mockito.mock(IndexWriter.class));
		IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		indexableFileSystem.setPath(FileUtilities.findFileRecursively(new File("."), Boolean.TRUE, "data").getAbsolutePath());
		IStrategy strategy = Mockito.mock(IStrategy.class);
		Mockito.when(strategy.preProcess(Mockito.any(IndexContext.class), Mockito.any(IndexableFileSystem.class), Mockito.any(File.class)))
				.thenReturn(Boolean.TRUE);
		indexableFileSystem.getStrategies().add(strategy);
		IndexableFilesystemHandler indexableHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);

		List<Future<?>> futures = indexableHandler.handle(indexContext, indexableFileSystem);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);

		Mockito.verify(strategy, Mockito.atLeastOnce()).preProcess(Mockito.any(IndexContext.class), Mockito.any(IndexableFileSystem.class),
				Mockito.any(File.class));
	}
}