package ikube.scheduling.schedule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.cluster.IClusterManager;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClassificationReportScheduleTest extends AbstractTest {

	private ISearcherService searcherService;
	private IClusterManager clusterManager;
	private ClassificationReportSchedule classificationReportSchedule;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		Mockit.setUpMocks();
		searcherService = Mockito.mock(ISearcherService.class);
		clusterManager = Mockito.mock(IClusterManager.class);
		classificationReportSchedule = new ClassificationReportSchedule();
		Deencapsulation.setField(classificationReportSchedule, "searcherService", searcherService);
		Deencapsulation.setField(classificationReportSchedule, "clusterManager", clusterManager);

		File file = FileUtilities.findFileRecursively(new File("."), "twitter.results.xml");
		String xml = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(searcherService.searchComplex(anyString(), any(String[].class), any(String[].class), any(String[].class), anyBoolean(), anyInt(), anyInt()))
				.thenReturn(results);
	}

	@Test
	public void getResults() {
		int results = classificationReportSchedule.getResults(null, null);
		assertEquals("There should be a lot of results : ", 82328, results);
	}

	@Test
	public void getTime() {
		String time = classificationReportSchedule.getTime(System.currentTimeMillis());
		logger.info("Time : " + time);
		// This verifies that it is a double figure
		Double.parseDouble(time);
	}

	@Test
	public void getRange() {
		long startTime = System.currentTimeMillis();
		String timeRange = classificationReportSchedule.getTimeRange(startTime, startTime + 60000);
		logger.info("Time range : " + timeRange);
		assertTrue(timeRange.contains("-"));
	}

	@Test
	public void run() {
		// TODO Verify the results
		// when(clusterManager.put(any(Object.class), any(Serializable.class)));
		classificationReportSchedule.run();
	}

}