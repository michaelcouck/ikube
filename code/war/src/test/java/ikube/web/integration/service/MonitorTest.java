package ikube.web.integration.service;

import static org.junit.Assert.assertEquals;
import ikube.service.IMonitorService;
import ikube.web.Base;
import ikube.web.service.Monitor;
import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MonitorTest extends Base {

	private Monitor monitor;

	@Before
	public void before() {
		monitor = new Monitor();
	}

	@Test
	public void fields() throws Exception {
		IMonitorService monitorService = Mockito.mock(IMonitorService.class);
		Deencapsulation.setField(monitor, monitorService);
		Mockito.when(monitorService.getIndexFieldNames(Mockito.anyString())).thenReturn(new String[] { "one", "two", "three" });
		String fields = monitor.fields("indexName");
		logger.info("Fields : " + fields);
		assertEquals("The string should be a concatenation of the fields : ", "one;two;three", fields);
	}

}