package ikube.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.service.IMonitoringService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Mock this test.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class MonitoringServiceTest extends BaseTest {

	private IMonitoringService monitoringService = ApplicationContextManager.getBean(IMonitoringService.class);

	public MonitoringServiceTest() {
		super(MonitoringServiceTest.class);
	}

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexContextNames() {
		String[] indexContextNames = monitoringService.getIndexContextNames();
		assertNotNull(indexContextNames);
		assertTrue(indexContextNames.length > 0);
	}

	@Test
	public void getFieldNames() {
		String[] indexContextNames = monitoringService.getIndexContextNames();
		for (String indexContextName : indexContextNames) {
			IndexContext indexContext = ApplicationContextManager.getBean(indexContextName);
			logger.info("Index context name : " + indexContextName);
			String[] fieldNames = monitoringService.getIndexFieldNames(indexContext.getIndexName());
			assertTrue(fieldNames.length > 0);
			for (String fieldName : fieldNames) {
				logger.debug("        : field name : " + fieldName);
			}
		}
		String[] indexFieldNames = monitoringService.getIndexFieldNames(IConstants.IKUBE);
		for (String indexfieldName : indexFieldNames) {
			logger.info("Field name : " + indexfieldName);
		}
		assertEquals("The first field name should be the content : ", "content", indexFieldNames[0]);
		assertEquals("The second field name should be the id : ", "id", indexFieldNames[1]);
		assertEquals("The third field name should be the title : ", "title", indexFieldNames[2]);
	}

	@Test
	public void getIndexSize() throws Exception {
		createIndex(indexContext, "The strings to index");
		long indexSize = monitoringService.getIndexSize(indexContext.getIndexName());
		logger.info("Index size : " + indexSize);
		assertTrue("There should be some data in the index : ", indexSize > 0);
	}

	@Test
	public void getIndexDocuments() throws Exception {
		createIndex(indexContext, "The strings to index");
		long indexDocuments = monitoringService.getIndexDocuments(indexContext.getIndexName());
		logger.info("Index documents : " + indexDocuments);
		assertEquals("There should be at least one document in the index : ", 2, indexDocuments, 1);
	}

	@Test
	public void getIndexableFieldNames() {
		String[] indexableFieldNames = monitoringService.getIndexableFieldNames("ikube.email");
		logger.info("Indexable field names : " + Arrays.asList(indexableFieldNames));
		assertEquals("The email is the first field : ", "email", indexableFieldNames[0]);
		assertEquals("The identifier is the second field : ", "identifier", indexableFieldNames[1]);
		assertEquals("The title is the third field : ", "title", indexableFieldNames[2]);

		indexableFieldNames = monitoringService.getIndexableFieldNames("geoname");
		logger.info("Indexable field names : " + Arrays.asList(indexableFieldNames));
		assertEquals("The alternatenames is the first field : ", "alternatenames", indexableFieldNames[0]);
	}

}