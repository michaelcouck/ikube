package ikube.toolkit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.action.IndexDelta;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.interceptor.IRuleInterceptor;
import ikube.mock.IndexManagerMock;
import ikube.model.IndexContext;

import java.util.List;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.document.Document;
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
public class AopTest {

	@Cascading
	private Document document;
	private String[] configLocations = { "./spring/spring-aop.xml", "./spring/spring-desktop.xml", "./spring/spring-beans.xml" };

	@Before
	public void before() {
		Mockit.setUpMocks(IndexManagerMock.class);
		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);
		ApplicationContextManager.getApplicationContext(configLocations);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(IndexManager.class);
		ApplicationContextManager.closeApplicationContext();
	}

	@Test
	public void handlerInterceptor() throws Exception {
		// IndexContext indexContext = ApplicationContextManager.getBean("desktop");
		// IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		// IndexableFilesystemHandler indexableHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class.getName());
		//
		// indexContext.setIndexWriters(mock(IndexWriter.class));
		// IStrategy strategy = mock(IStrategy.class);
		// when(strategy.aroundProcess(any(IndexContext.class), any(IndexableFileSystem.class), any(File.class))).thenReturn(Boolean.TRUE);
		// Deencapsulation.setField(document, new ArrayList<Fieldable>());
		// indexableFileSystem.setStrategies(Arrays.asList(strategy));
		//
		// File file = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "default.results.xml");
		// indexableHandler.handleResource(indexContext, indexableFileSystem, document, file);
		//
		// verify(strategy, atLeastOnce()).aroundProcess(any(IndexContext.class), any(IndexableFileSystem.class), any(Document.class),
		// any(Object[].class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void actionInterceptor() throws Exception {
		IRuleInterceptor ruleInterceptor = ApplicationContextManager.getBean(IRuleInterceptor.class);
		IClusterManager clusterManager = mock(IClusterManager.class);
		Deencapsulation.setField(ruleInterceptor, clusterManager);
		IndexDelta indexDelta = ApplicationContextManager.getBean(IndexDelta.class);
		IndexContext<?> indexContext = mock(IndexContext.class);
		when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
		indexDelta.preExecute(indexContext);

		verify(indexContext, atLeastOnce()).setHashes(any(List.class));
	}
}