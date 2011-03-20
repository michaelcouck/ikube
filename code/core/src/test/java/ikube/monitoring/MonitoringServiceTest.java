package ikube.monitoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.IndexContext;
import ikube.monitoring.IMonitoringService;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class MonitoringServiceTest extends BaseTest {

	private IMonitoringService monitoringService = ApplicationContextManager.getBean(IMonitoringService.class);

	public MonitoringServiceTest() {
		super(MonitoringServiceTest.class);
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
			logger.debug("Index context name : " + indexContextName);
			String[] fieldNames = monitoringService.getIndexFieldNames(indexContext.getIndexName());
			assertTrue(fieldNames.length > 0);
			for (String fieldName : fieldNames) {
				logger.debug("        : field name : " + fieldName);
			}
		}
	}

}
