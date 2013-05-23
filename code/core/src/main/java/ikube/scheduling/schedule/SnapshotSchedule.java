package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.scheduling.Schedule;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This schedule will take a snapshot of various system states periodically, including the cpu, how many searches there have been on all the
 * indexes etc. Snapshots are then persisted to the database, and cleaned from time to time in the {@link Purge} action so the database
 * doesn't fill up. Typically we only need a few snapshots and not two weeks worth. This schedule should run every minute.
 * 
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
public class SnapshotSchedule extends Schedule {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotSchedule.class);

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
	public void run() {
		Server server = clusterManager.getServer();
		setServerStatistics(server);
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			try {
				IndexContext indexContext = mapEntry.getValue();

				Snapshot snapshot = new Snapshot();

				snapshot.setAvailableProcessors(server.getProcessors());
				snapshot.setSystemLoad(server.getAverageCpuLoad());

				snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));
				snapshot.setDocsPerMinute(getDocsPerMinute(indexContext, snapshot));
				snapshot.setIndexContext(indexContext.getName());
				snapshot.setIndexSize(IndexManager.getIndexSize(indexContext));
				snapshot.setLatestIndexTimestamp(IndexManager.getLatestIndexDirectoryDate(indexContext));
				snapshot.setNumDocs(IndexManager.getNumDocs(indexContext));
				snapshot.setSearchesPerMinute(getSearchesPerMinute(indexContext, snapshot));
				snapshot.setTotalSearches(getTotalSearchesForIndex(indexContext));

				dataBase.persist(snapshot);
				String[] names = new String[] { "indexContext" };
				Object[] values = new Object[] { indexContext.getName() };
				List<Snapshot> snapshots = dataBase.find(Snapshot.class, Snapshot.SELECT_SNAPSHOTS_ORDER_BY_TIMESTAMP_DESC, names, values,
						0, 90);
				snapshots = new ArrayList<Snapshot>(snapshots);
				// We have the last snapshots, now reverse the order for the gui
				Comparator<Snapshot> comparator = new Comparator<Snapshot>() {
					@Override
					public int compare(Snapshot o1, Snapshot o2) {
						return o1.getTimestamp().compareTo(o2.getTimestamp());
					}
				};
				Collections.reverseOrder(comparator);
				indexContext.setSnapshots(snapshots);

				// Find the last snapshot and put it in the action if there is one
				// executing on the index context
				for (final Action action : server.getActions()) {
					if (action.getIndexName().equals(indexContext.getName())) {
						action.setSnapshot(snapshot);
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception persisting snapshot : ", e);
			}
		}
		clusterManager.put(server.getAddress(), server);
	}

	protected void setServerStatistics(final Server server) {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		server.setArchitecture(operatingSystemMXBean.getArch());
		server.setProcessors(operatingSystemMXBean.getAvailableProcessors());
		server.setAverageCpuLoad(operatingSystemMXBean.getSystemLoadAverage());
		server.setFreeMemory(Runtime.getRuntime().freeMemory() / IConstants.MILLION);
		server.setMaxMemory(Runtime.getRuntime().maxMemory() / IConstants.MILLION);
		server.setTotalMemory(Runtime.getRuntime().totalMemory() / IConstants.MILLION);
		server.setThreadsRunning(ThreadUtilities.isInitialized());
		try {
			long availableDiskSpace = FileSystemUtils.freeSpaceKb("/") / IConstants.MILLION;
			server.setFreeDiskSpace(availableDiskSpace);
		} catch (IOException e) {
			LOGGER.error("Exception accessing the disk space : ", e);
		}
		setLogTail(server);
	}

	void setLogTail(final Server server) {
		File logFile = FileUtilities.findFileRecursively(new File("./" + IConstants.IKUBE), IConstants.IKUBE_LOG);
		if (logFile != null) {
			RandomAccessFile inputStream = null;
			try {
				inputStream = new RandomAccessFile(logFile, "r");
				// 1000000
				int fileLength = (int) logFile.length();
				// 900000
				int offset = Math.max(fileLength - (IConstants.MILLION / 10), 0);
				// 100000
				int lengthToRead = Math.max(0, fileLength - offset);
				// 100000
				byte[] bytes = new byte[lengthToRead];
				LOGGER.info("Offset : " + offset + ", file length : " + logFile.length() + ", to read : " + lengthToRead);
				inputStream.seek(offset);
				inputStream.read(bytes, 0, lengthToRead);
				server.setLogTail(new String(bytes));
			} catch (FileNotFoundException e) {
				LOGGER.error("Log file not found : ", e);
			} catch (Exception e) {
				LOGGER.error("Error reading log file : ", e);
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (Exception e) {
					LOGGER.error("Exception closing the file reader on the log file : ", e);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected long getTotalSearchesForIndex(final IndexContext indexContext) {
		int totalSearchesForIndex = 0;
		// TODO Re-do this, there could potentially be millions of searches
		List<Search> searches = dataBase.find(Search.class, Search.SELECT_FROM_SEARCH_BY_INDEX_NAME, new String[] { "indexName" },
				new Object[] { indexContext.getIndexName() }, 0, 1000000);
		for (Search search : searches) {
			totalSearchesForIndex += search.getCount();
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
		return (long) ((snapshot.getTotalSearches() - previous.getTotalSearches()) / ratio);
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
		return docsPerMinute < 0 ? 0 : Math.min(docsPerMinute, 1000000);
	}

}