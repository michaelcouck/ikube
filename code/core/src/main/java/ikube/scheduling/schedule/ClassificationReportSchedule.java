package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.scheduling.Schedule;
import ikube.search.ISearcherService;
import ikube.search.Search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael Couck
 * @since 18.08.13
 * @version 01.00
 */
public class ClassificationReportSchedule extends Schedule {

	static final Logger LOGGER = LoggerFactory.getLogger(ClassificationReportSchedule.class);

	/** The index to extract the report from. */
	private String indexName = "twitter";
	/** The window of time to extract the report in. */
	private long timeFrame = 90 * 1000 * 60;
	/** The language of the documents to extract for. */
	private String language = "en";
	/** The timestamp field for the index. */
	private String timestampFieldName = "created-at";
	/** The target field to search for, in the base case it is the classification field. */
	private String targetField = IConstants.CLASSIFICATION;
	/** The target variable to search for, i.e. positive and negative in the sentiment analysis. */
	private String[] targetFieldValues = { IConstants.POSITIVE, IConstants.NEGATIVE };

	@Autowired
	private IClusterManager clusterManager;
	@Autowired
	private ISearcherService searcherService;

	/**
	 * {@inheritDoc}
	 * 
	 * The format for Google visualization is: [["Time interval", "Positive", "Negative"], ["10.45", 400, 300], ["10.46", 200, 800], ["10.47", 1500, 100]]
	 */
	@Override
	public void run() {
		// Create the array of ranges for the search
		int index = 0;
		Object[][] data = new Object[(int) (timeFrame / 1000 / 60) + 2][3];
		// Set the header for the data
		data[index] = new Object[] { "Times", "Positive", "Negative" };
		long startTime = System.currentTimeMillis() - timeFrame;
		while (startTime < System.currentTimeMillis()) {
			index++;
			long endTime = startTime + (1000 * 60);
			String time = getTime(startTime);

			int subIndex = 0;
			data[index] = new Object[3];
			data[index][subIndex] = time;

			for (final String targetFieldValue : targetFieldValues) {
				subIndex++;
				String timeRange = getTimeRange(startTime, endTime);
				int resultsSize = getResults(timeRange, targetFieldValue);
				data[index][subIndex] = resultsSize;
			}
			startTime = endTime;
		}

		clusterManager.put(IConstants.INDEX_NAME, data);
	}

	int getResults(final String timeRange, final String targetFieldValue) {
		String[] searchStrings = { language, timeRange, targetFieldValue };
		String[] searchFields = { IConstants.LANGUAGE, timestampFieldName, targetField };
		String[] typeFields = { Search.TypeField.STRING.fieldType(), Search.TypeField.RANGE.fieldType(), Search.TypeField.STRING.fieldType() };
		String[] sortFields = {};
		ArrayList<HashMap<String, String>> results = searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, false, 0, 10);
		HashMap<String, String> statistics = results.get(results.size() - 1);
		int total = Integer.parseInt(statistics.get(IConstants.TOTAL));
		return total;
	}

	String getTime(final long startTime) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(startTime));
		int hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
		int minute = gregorianCalendar.get(Calendar.MINUTE);
		return new StringBuilder().append(hour).append(".").append(minute).toString();
	}

	String getTimeRange(final long startTime, final long endTime) {
		return new StringBuilder().append(startTime).append("-").append(endTime).toString();
	}

}