package ikube.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.service.IMonitorWebService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class MonitorWebServiceTest extends BaseTest {

	private IMonitorWebService monitorWebService;

	public MonitorWebServiceTest() {
		super(MonitorWebServiceTest.class);
	}

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		monitorWebService = ApplicationContextManager.getBean(IMonitorWebService.class);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexContextNames() {
		String[] indexContextNames = monitorWebService.getIndexContextNames();
		assertNotNull(indexContextNames);
		assertTrue(indexContextNames.length > 0);
	}

	@Test
	public void getIndexFieldNames() {
		String[] indexContextNames = monitorWebService.getIndexContextNames();
		for (String indexContextName : indexContextNames) {
			IndexContext<?> indexContext = ApplicationContextManager.getBean(indexContextName);
			logger.info("Index context name : " + indexContextName);
			String[] fieldNames = monitorWebService.getIndexFieldNames(indexContext.getIndexName());
			assertTrue(fieldNames.length > 0);
			for (String fieldName : fieldNames) {
				logger.debug("        : field name : " + fieldName);
			}
		}
		String[] indexFieldNames = monitorWebService.getIndexFieldNames(IConstants.IKUBE);
		for (String indexfieldName : indexFieldNames) {
			logger.info("Field name : " + indexfieldName);
		}
		List<String> indexFieldNamesList = Arrays.asList(indexFieldNames);
		assertTrue("The id field should be in the Ikube index : ", indexFieldNamesList.contains(IConstants.ID));
		assertTrue("The title field should be in the Ikube index : ", indexFieldNamesList.contains(IConstants.TITLE));
		assertTrue("The content field should be in the Ikube index : ", indexFieldNamesList.contains(IConstants.CONTENT));
	}

	@Test
	public void getIndexSize() throws Exception {
		createIndex(indexContext, "The strings to index");
		long indexSize = monitorWebService.getIndexSize(indexContext.getIndexName());
		logger.info("Index size : " + indexSize);
		assertTrue("There should be some data in the index : ", indexSize > 0);
	}

	@Test
	public void getIndexDocuments() throws Exception {
		createIndex(indexContext, "The strings to index");
		long indexDocuments = monitorWebService.getIndexDocuments(indexContext.getIndexName());
		logger.info("Index documents : " + indexDocuments);
		assertEquals("There should be at least one document in the index : ", 2, indexDocuments, 1);
	}

	@Test
	public void getIndexableFieldNames() {
		String[] indexableFieldNames = monitorWebService.getIndexableFieldNames("ikube.email");
		logger.info("Indexable field names : " + Arrays.asList(indexableFieldNames));
		assertEquals("The email is the first field : ", "email", indexableFieldNames[0]);
		assertEquals("The identifier is the second field : ", "identifier", indexableFieldNames[1]);
		assertEquals("The title is the third field : ", "title", indexableFieldNames[2]);

		indexableFieldNames = monitorWebService.getIndexableFieldNames("geoname");
		logger.info("Indexable field names : " + Arrays.asList(indexableFieldNames));
		assertEquals("The countrycode is the first field : ", "countrycode", indexableFieldNames[0]);
	}

}