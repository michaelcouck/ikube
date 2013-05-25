package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.cluster.IMonitorService;
import ikube.cluster.MonitorService;
import ikube.database.IDataBase;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorServiceTest extends AbstractTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private IMonitorService monitorService;

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() {
		monitorService = new MonitorService();

		indexContext = new IndexContext<Object>();
		indexContext.setIndexName("indexName");
		indexContext.setIndexDirectoryPath("./indexes");
		List<Indexable<?>> indexables = new ArrayList<Indexable<?>>(Arrays.asList(new IndexableFileSystem()));
		indexContext.setChildren(indexables);

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		IDataBase dataBase = Mockito.mock(IDataBase.class);
		Deencapsulation.setField(monitorService, dataBase);

		List<IndexContext> indexContexts = new ArrayList<IndexContext>(Arrays.asList(indexContext));
		Mockito.when(dataBase.find(IndexContext.class, 0, Integer.MAX_VALUE)).thenReturn(indexContexts);
	}

	@Test
	public void getFieldNames() {
		String[] fieldNames = monitorService.getFieldNames(IndexableEmail.class);
		logger.info("Field names : " + Arrays.deepToString(fieldNames));
		assertEquals(
				"[idField, titleField, contentField, mailHost, username, password, port, protocol, secureSocketLayer, name, address, stored, analyzed, vectored, maxExceptions, id]",
				Arrays.deepToString(fieldNames));
		fieldNames = monitorService.getFieldNames(IndexContext.class);
		logger.info("Field names : " + Arrays.deepToString(fieldNames));
	}

	@Test
	public void getFieldDescriptions() {
		String[] descriptions = monitorService.getFieldDescriptions(IndexContext.class);
		logger.info("Descriptions : " + Arrays.deepToString(descriptions));
		assertTrue(Arrays.deepToString(descriptions).contains("This is the throttle in mili seconds that will slow down the indexing"));
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

	@Test
	public void getSetProperties() {
		String propertiesFilePath = "./properties/spring.properties";
		try {
			File file = FileUtilities.findFileRecursively(new File("."), "spring.properties");
			String contents = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
			FileUtilities.setContents(propertiesFilePath, contents.getBytes());

			File propertiesFile = new File(propertiesFilePath);

			Map<String, String> filesAndProperties = monitorService.getProperties();
			assertTrue(filesAndProperties.containsKey(propertiesFile.getAbsolutePath()));

			filesAndProperties.clear();
			String propertiesFileContents = "my-property=my-value\nanother-property=another-value";
			filesAndProperties.put(propertiesFile.getAbsolutePath(), propertiesFileContents);
			monitorService.setProperties(filesAndProperties);

			String propertiesFileContentsRead = FileUtilities.getContents(propertiesFile, Integer.MAX_VALUE).toString();
			assertEquals("The properties file should contain the contents int he map : ", propertiesFileContents,
					propertiesFileContentsRead);
		} finally {
			FileUtilities.deleteFile(new File("./properties"), 1);
		}
	}

}
