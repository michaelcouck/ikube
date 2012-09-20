package ikube.integration.service;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.database.IDataBase;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.service.IMonitorService;
import ikube.service.MonitorService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class MonitorServiceTest extends ATest {

	private IMonitorService monitorService;
	private IndexContext<?> indexContext;

	public MonitorServiceTest() {
		super(MonitorServiceTest.class);
	}

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() {
		monitorService = new MonitorService();

		indexContext = new IndexContext<Object>();
		indexContext.setIndexName("indexName");
		indexContext.setIndexDirectoryPath("./indexes");
		List<Indexable<?>> indexables = new ArrayList<Indexable<?>>(Arrays.asList(new IndexableFileSystem()));
		indexContext.setIndexables(indexables);

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		IDataBase dataBase = Mockito.mock(IDataBase.class);
		Deencapsulation.setField(monitorService, dataBase);

		List<IndexContext> indexContexts = new ArrayList<IndexContext>(Arrays.asList(indexContext));
		Mockito.when(dataBase.find(IndexContext.class, 0, Integer.MAX_VALUE)).thenReturn(indexContexts);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexFieldNames() {
		logger.info("Index context : " + indexContext);
		String[] fieldNames = monitorService.getIndexFieldNames(indexContext.getIndexName());
		assertTrue(fieldNames.length > 0);

		List<String> indexFieldNamesList = Arrays.asList(fieldNames);
		assertTrue("The id field should be in the Ikube index : ", indexFieldNamesList.contains("nameFieldName"));
		assertTrue("The title field should be in the Ikube index : ", indexFieldNamesList.contains("pathFieldName"));
		assertTrue("The content field should be in the Ikube index : ", indexFieldNamesList.contains("lastModifiedFieldName"));
	}

}