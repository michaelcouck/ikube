package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mockit.Mockit;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class MonitorServiceTest extends AbstractTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private IMonitorService monitorService;

	@Before
	public void before() {
		monitorService = new MonitorService();

		List<Indexable<?>> indexables = new ArrayList<Indexable<?>>(Arrays.asList(new IndexableFileSystem()));
		indexContext.setChildren(indexables);

		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);

		ApplicationContextManagerMock.INDEX_CONTEXT = indexContext;
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getFieldNames() {
		String[] fieldNames = monitorService.getFieldNames(IndexableEmail.class);
		logger.info("Field names : " + Arrays.deepToString(fieldNames));
		assertEquals(
				"[idField, titleField, contentField, mailHost, username, password, port, protocol, secureSocketLayer, name, address, stored, analyzed, vectored, maxExceptions, threads, id]",
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
	public void getIndexFieldNames() throws Exception {
		MultiSearcher multiSearcher = null;
		try {
			File indexDirectory = createIndexFileSystem(indexContext, "Hello world");
			logger.info("Index directory : " + indexDirectory.getAbsolutePath());

			Directory directory = FSDirectory.open(indexDirectory);
			Searchable[] searchables = new Searchable[] { new IndexSearcher(directory) };
			multiSearcher = new MultiSearcher(searchables);
			Mockito.when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);

			String[] fieldNames = monitorService.getIndexFieldNames(indexContext.getIndexName());
			assertTrue(fieldNames.length > 0);
		} finally {
			multiSearcher.close();
		}
	}

	@Test
	@Ignore
	public void getSetProperties() throws IOException {
		File propertiesFile = null;
		try {
			File file = FileUtilities.findFileRecursively(new File("."), "spring.properties");
			String contents = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();

			propertiesFile = FileUtilities.getOrCreateFile("./properties/spring.properties");
			FileUtilities.setContents(propertiesFile, contents.getBytes());

			Map<String, String> filesAndProperties = monitorService.getProperties();
			logger.info("Files found : " + filesAndProperties.keySet());
			String cleanPath = FileUtilities.cleanFilePath(propertiesFile.getAbsolutePath());
			assertTrue(filesAndProperties.containsKey(cleanPath));

			filesAndProperties.clear();
			String propertiesFileContents = "my-property=my-value\nanother-property=another-value";
			filesAndProperties.put(propertiesFile.getAbsolutePath(), propertiesFileContents);
			monitorService.setProperties(filesAndProperties);

			String propertiesFileContentsRead = FileUtilities.getContents(propertiesFile, Integer.MAX_VALUE).toString();
			assertEquals("The properties file should contain the contents in the map : ", propertiesFileContents, propertiesFileContentsRead);
		} finally {
			if (propertiesFile != null) {
				FileUtilities.deleteFile(propertiesFile.getParentFile(), 1);
			}
		}
	}

}
