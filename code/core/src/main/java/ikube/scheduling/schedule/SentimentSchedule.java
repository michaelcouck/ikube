package ikube.scheduling.schedule;

import ikube.cluster.IMonitorService;
import ikube.scheduling.Schedule;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class will generate sentiment data analytics from the collected data in the defined indexes.
 * 
 * @author Michael Couck
 * @since 01.07.13
 * @version 01.00
 */
public class SentimentSchedule extends Schedule {

	@Autowired
	private IMonitorService monitorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// TODO Implement me
		// Iterate over the indexes
		// Create sentiment based on the language
		// Create sentiment based on the country
		// Create sentiment based on gender
		// Correlate sentiment to weather for the area
		// Correlate sentiment to stock market for country
		// Correlate long term sentiment with time of day
		// Correlate long term sentiment with day of week
	}

}