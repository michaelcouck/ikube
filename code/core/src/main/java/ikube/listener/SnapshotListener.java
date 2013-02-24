package ikube.listener;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
public class SnapshotListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotListener.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleNotification(Event event) {
		if (!Event.PERFORMANCE.equals(event.getType())) {
			return;
		}
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		Server server = clusterManager.getServer();
		server.setArchitecture(operatingSystemMXBean.getArch());
		server.setProcessors(operatingSystemMXBean.getAvailableProcessors());
		server.setAverageCpuLoad(operatingSystemMXBean.getSystemLoadAverage());
		try {
			long availableDiskSpace = FileSystemUtils.freeSpaceKb("/") / IConstants.MILLION;
			server.setFreeDiskSpace(availableDiskSpace);
		} catch (IOException e) {
			LOGGER.error("Exception accessing the disk space : ", e);
		}
		server.setFreeMemory(Runtime.getRuntime().freeMemory() / IConstants.MILLION);
		server.setMaxMemory(Runtime.getRuntime().maxMemory() / IConstants.MILLION);
		server.setTotalMemory(Runtime.getRuntime().totalMemory() / IConstants.MILLION);
		clusterManager.putObject(server.getAddress(), server);

		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			try {
				IndexContext indexContext = mapEntry.getValue();

				Snapshot snapshot = new Snapshot();

				snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));
				snapshot.setIndexSize(IndexManager.getIndexSize(indexContext));
				snapshot.setNumDocs(IndexManager.getNumDocsIndexWriter(indexContext));
				snapshot.setLatestIndexTimestamp(IndexManager.getLatestIndexDirectoryDate(indexContext));
				snapshot.setDocsPerMinute(getDocsPerMinute(indexContext, snapshot));
				snapshot.setTotalSearches(getTotalSearchesForIndex(indexContext));
				snapshot.setSearchesPerMinute(getSearchesPerMinute(indexContext, snapshot));
				snapshot.setSystemLoad(operatingSystemMXBean.getSystemLoadAverage());
				snapshot.setAvailableProcessors(operatingSystemMXBean.getAvailableProcessors());

				dataBase.persist(snapshot);
				indexContext.getSnapshots().add(snapshot);
				if (indexContext.getSnapshots().size() > IConstants.MAX_SNAPSHOTS) {
					LinkedList<Snapshot> snapshots = new LinkedList<Snapshot>();
					snapshots.addAll(indexContext.getSnapshots());
					while (snapshots.size() > (int) (IConstants.MAX_SNAPSHOTS * 0.25d)) {
						snapshots.removeFirst();
					}
					indexContext.setSnapshots(snapshots);
				}
			} catch (Exception e) {
				LOGGER.error("Exception persisting snapshot : ", e);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected long getTotalSearchesForIndex(final IndexContext indexContext) {
		int totalSearchesForIndex = 0;
		List<Search> searches = null;
		try {
			searches = dataBase.find(Search.class, Search.SELECT_FROM_SEARCH_BY_INDEX_NAME, new String[] { "indexName" },
					new Object[] { indexContext.getIndexName() }, 0, Integer.MAX_VALUE);
			for (Search search : searches) {
				totalSearchesForIndex += search.getCount();
			}
		} catch (Exception e) {
			LOGGER.info("Error getting the search results : ", e);
		}
		return totalSearchesForIndex;
	}

	protected long getSearchesPerMinute(final IndexContext<?> indexContext, final Snapshot snapshot) {
		List<Snapshot> snapshots = indexContext.getSnapshots();
		if (snapshots == null || snapshots.size() < 1) {
			return 0;
		}
		Snapshot previous = snapshots.get(snapshots.size() - 1);
		double interval = snapshot.getTimestamp().getTime() - previous.getTimestamp().getTime();
		double ratio = interval / 60000;
		long searchesPerMinute = (long) ((snapshot.getTotalSearches() - previous.getTotalSearches()) / ratio);
		return searchesPerMinute;
	}

	protected long getDocsPerMinute(final IndexContext<?> indexContext, final Snapshot snapshot) {
		List<Snapshot> snapshots = indexContext.getSnapshots();
		if (snapshots == null || snapshots.size() == 0) {
			return 0;
		}
		Snapshot previous = snapshots.get(snapshots.size() - 1);
		double interval = snapshot.getTimestamp().getTime() - previous.getTimestamp().getTime();
		double ratio = interval / 60000;
		long docsPerMinute = (long) ((snapshot.getNumDocs() - previous.getNumDocs()) / ratio);
		// LOGGER.info("Docs per minute : " + indexContext.getName() + ", " + indexContext.hashCode() + ", " + docsPerMinute);
		return docsPerMinute < 0 ? 0 : Math.min(docsPerMinute, 1000000);
	}

}