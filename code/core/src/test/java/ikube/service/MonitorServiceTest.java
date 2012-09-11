package ikube.service;

import static org.junit.Assert.*;

import ikube.ATest;
import ikube.model.IndexableEmail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorServiceTest extends ATest {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private IMonitorService monitorService;

	public MonitorServiceTest() {
		super(MonitorServiceTest.class);
	}

	@Before
	public void before() {
		monitorService = new MonitorService();
	}

	@Test
	public void getFieldNames() {
		String[] fieldNames = monitorService.getFieldNames(IndexableEmail.class);
		logger.info("Field names : " + Arrays.deepToString(fieldNames));
		assertEquals(
				"[idField, titleField, contentField, mailHost, username, password, port, protocol, secureSocketLayer, name, address, stored, analyzed, vectored, id]",
				Arrays.deepToString(fieldNames));
	}

}
