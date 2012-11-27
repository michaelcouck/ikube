package ikube.listener;

import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;

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
public class CpuLoadListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CpuLoadListener.class);

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
	public void handleNotification(Event event) {
		if (!active) {
			return;
		}
		if (Event.PERFORMANCE.equals(event.getType())) {
			boolean increaseThrottle = Boolean.TRUE;
			boolean decreaseThrottle = Boolean.TRUE;
			Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
			for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
				IndexContext indexContext = mapEntry.getValue();
				// We check if the cpu load has been more than 1 per core for a period
				// of ten minutes. If so then we increase the throttle for all index contexts
				// by one millisecond
				List<Snapshot> snapshots = indexContext.getSnapshots();
				if (snapshots != null && snapshots.size() > 0) {
					for (int i = snapshots.size() - 1, j = 0; i > 0 && j < period; i--, j++) {
						Snapshot snapshot = snapshots.get(i);
						if (snapshot.getSystemLoad() / snapshot.getAvailableProcessors() < threshold) {
							increaseThrottle &= Boolean.FALSE;
						} else {
							decreaseThrottle &= Boolean.FALSE;
						}
					}
				}
			}
			if (increaseThrottle) {
				for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
					IndexContext indexContext = mapEntry.getValue();
					LOGGER.info("Increasing throttle for index context : " + indexContext.getName());
					indexContext.setThrottle(indexContext.getThrottle() + 1);
				}
			}
			if (decreaseThrottle) {
				for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
					IndexContext indexContext = mapEntry.getValue();
					LOGGER.info("Decreasing throttle for index context : " + indexContext.getName());
					if (indexContext.getThrottle() > 0) {
						indexContext.setThrottle(indexContext.getThrottle() - 1);
					}
				}
			}
		}
	}

}