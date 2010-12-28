package ikube.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

public class MonitoringServiceTest extends BaseTest {

	private IMonitoringService monitoringService = ApplicationContextManager.getBean(IMonitoringService.class);

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
			logger.debug("Index context name : " + indexContextName);
			String[] fieldNames = monitoringService.getFieldNames(indexContextName);
			assertTrue(fieldNames.length > 0);
			for (String fieldName : fieldNames) {
				logger.debug("        : field name : " + fieldName);
			}
		}
	}

}
