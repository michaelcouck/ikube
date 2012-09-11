package ikube.integration.service;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.integration.AbstractIntegration;
import ikube.service.IMonitorService;
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

	private IMonitorService monitorService;

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(realIndexContext.getIndexDirectoryPath()), 1);
		monitorService = ApplicationContextManager.getBean(IMonitorService.class);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(realIndexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexFieldNames() {
		String[] fieldNames = monitorService.getIndexFieldNames(IConstants.GEOSPATIAL);
		assertTrue(fieldNames.length > 0);
		for (String fieldName : fieldNames) {
			logger.debug("        : field name : " + fieldName);
		}
		String[] indexFieldNames = monitorService.getIndexFieldNames(IConstants.IKUBE);
		for (String indexfieldName : indexFieldNames) {
			logger.info("Field name : " + indexfieldName);
		}
		List<String> indexFieldNamesList = Arrays.asList(indexFieldNames);
		assertTrue("The id field should be in the Ikube index : ", indexFieldNamesList.contains(IConstants.ID));
		assertTrue("The title field should be in the Ikube index : ", indexFieldNamesList.contains(IConstants.TITLE));
		assertTrue("The content field should be in the Ikube index : ", indexFieldNamesList.contains(IConstants.CONTENT));
	}

}