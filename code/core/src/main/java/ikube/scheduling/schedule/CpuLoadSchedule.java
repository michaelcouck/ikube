package ikube.scheduling.schedule;

import ikube.cluster.IMonitorService;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.scheduling.Schedule;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class will check whether the cpu load on the server is constantly too high. If so then the throttle for the index contexts is
 * increased, slowing the indexing down and relieving the load on the cpu.
 * 
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
public class CpuLoadSchedule extends Schedule {

	private static final Logger LOGGER = LoggerFactory.getLogger(CpuLoadSchedule.class);

	@Value("${cpu.load.period}")
	private int period = 10;
	@Value("${cpu.load.active}")
	private boolean active = Boolean.TRUE;
	@Value("${cpu.load.threshold}")
	private double threshold = 1.0;

	@Autowired
	private IMonitorService monitorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() {
		if (!active) {
			return;
		}
		boolean increaseThrottle = Boolean.FALSE;
		boolean decreaseThrottle = Boolean.FALSE;
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext indexContext = mapEntry.getValue();
			// We check if the cpu load has been more than 1 per core for a period
			// of x minutes. If so then we increase the throttle for all index contexts
			// by one millisecond
			List<Snapshot> snapshots = indexContext.getSnapshots();
			if (snapshots != null && snapshots.size() >= period) {
				boolean allOver = true;
				boolean allUnder = true;
				for (int i = snapshots.size() - 1, j = 0; i > 0 && j < period; i--, j++) {
					Snapshot snapshot = snapshots.get(i);
					allOver &= snapshot.getSystemLoad() / snapshot.getAvailableProcessors() > threshold;
					allUnder &= snapshot.getSystemLoad() / snapshot.getAvailableProcessors() < threshold;
				}
				increaseThrottle |= allOver;
				decreaseThrottle |= allUnder;
			}
		}
		if (increaseThrottle) {
			for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
				IndexContext indexContext = mapEntry.getValue();
				LOGGER.debug("Increasing throttle for index context : " + indexContext.getName());
				indexContext.setThrottle(indexContext.getThrottle() + 1);
			}
		}
		if (decreaseThrottle) {
			for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
				IndexContext indexContext = mapEntry.getValue();
				if (indexContext.getThrottle() > 0) {
					LOGGER.debug("Decreasing throttle for index context : " + indexContext.getName());
					indexContext.setThrottle(indexContext.getThrottle() - 1);
				}
			}
		}
	}

}