package ikube.integration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.integration.AbstractIntegration;
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
public class MonitorWebServiceIntegration extends AbstractIntegration {

	private IMonitorWebService monitorWebService;

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(realIndexContext.getIndexDirectoryPath()), 1);
		monitorWebService = ApplicationContextManager.getBean(IMonitorWebService.class);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(realIndexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexContextNames() {
		String[] indexContextNames = monitorWebService.getIndexContextNames();
		assertNotNull(indexContextNames);
		assertTrue(indexContextNames.length > 0);
	}

	@Test
	public void getIndexFieldNames() {
		String[] fieldNames = monitorWebService.getIndexFieldNames(IConstants.GEOSPATIAL);
		assertTrue(fieldNames.length > 0);
		for (String fieldName : fieldNames) {
			logger.debug("        : field name : " + fieldName);
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
		createIndex(realIndexContext, "The strings to index");
		long indexSize = monitorWebService.getIndexSize(realIndexContext.getIndexName());
		logger.info("Index size : " + indexSize);
		assertTrue("There should be some data in the index : ", indexSize > 0);
	}

	@Test
	public void getIndexDocuments() throws Exception {
		createIndex(realIndexContext, "The strings to index");
		long indexDocuments = monitorWebService.getIndexDocuments(realIndexContext.getIndexName());
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
	}

}