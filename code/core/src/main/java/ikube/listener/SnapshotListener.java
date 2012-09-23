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
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
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
		if (Event.PERFORMANCE.equals(event.getType())) {
			Server server = clusterManager.getServer();
			OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
			server.setArchitecture(operatingSystemMXBean.getArch());
			server.setProcessors(operatingSystemMXBean.getAvailableProcessors());
			server.setAverageCpuLoad(operatingSystemMXBean.getSystemLoadAverage());
			try {
				long availableDiskSpace = FileSystemUtils.freeSpaceKb("/") / 1000;
				server.setFreeDiskSpace(availableDiskSpace);
			} catch (IOException e) {
				LOGGER.error("Exception accessing the disk space : ", e);
			}
			server.setFreeMemory(Runtime.getRuntime().freeMemory());
			server.setMaxMemory(Runtime.getRuntime().maxMemory());
			server.setTotalMemory(Runtime.getRuntime().totalMemory());
			clusterManager.putObject(server.getAddress(), server);

			Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
			for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
				IndexContext indexContext = mapEntry.getValue();
				try {
					// long availableDiskSpace = FileSystemUtils.freeSpaceKb(indexContext.getIndexDirectoryPath()) / 1000;
					// indexContext.setAvailableDiskSpace(availableDiskSpace);
				} catch (Exception e) {
					LOGGER.error("Exception accessing the disk space : ", e);
				}

				Snapshot snapshot = new Snapshot();
				snapshot.setIndexSize(getIndexSize(indexContext));
				snapshot.setNumDocs(getNumDocs(indexContext));
				snapshot.setTimestamp(System.currentTimeMillis());
				snapshot.setLatestIndexTimestamp(getLatestIndexDirectoryDate(indexContext));
				snapshot.setDocsPerMinute(getDocsPerMinute(indexContext, snapshot));
				snapshot.setTotalSearches(getTotalSearchesForIndex(indexContext));
				// LOGGER.info("Setting searches per minute : " + getSearchesPerMinute(indexContext, snapshot));
				snapshot.setSearchesPerMinute(getSearchesPerMinute(indexContext, snapshot));

				dataBase.persist(snapshot);
				indexContext.getSnapshots().add(snapshot);
				if (indexContext.getSnapshots().size() > IConstants.MAX_SNAPSHOTS) {
					List<Snapshot> subListToRemove = indexContext.getSnapshots().subList(0,
							(int) (((double) IConstants.MAX_SNAPSHOTS) * 0.25));
					indexContext.getSnapshots().removeAll(subListToRemove);
				}
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
		double interval = snapshot.getTimestamp() - previous.getTimestamp();
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
		double interval = snapshot.getTimestamp() - previous.getTimestamp();
		double ratio = interval / 60000;
		long docsPerMinute = (long) ((snapshot.getNumDocs() - previous.getNumDocs()) / ratio);
		if (docsPerMinute < 0) {
			docsPerMinute = 0;
		}
		return docsPerMinute < 50000 ? docsPerMinute : 0;
	}

	protected Date getLatestIndexDirectoryDate(final IndexContext<?> indexContext) {
		long timestamp = 0;
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory != null) {
			String name = latestIndexDirectory.getName();
			if (StringUtils.isNumeric(name)) {
				timestamp = Long.parseLong(name);
			}
		}
		return new Date(timestamp);
	}

	protected long getNumDocs(final IndexContext<?> indexContext) {
		long numDocs = 0;
		IndexWriter indexWriter = indexContext.getIndexWriter();
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		if (indexWriter != null) {
			try {
				numDocs = (int) indexWriter.numDocs();
			} catch (IOException e) {
				LOGGER.error("Exception getting the number of documents in the index : " + this, e);
			}
		} else if (numDocs == 0 && multiSearcher != null) {
			for (Searchable searchable : multiSearcher.getSearchables()) {
				numDocs += ((IndexSearcher) searchable).getIndexReader().numDocs();
			}
		}
		return numDocs;
	}

	protected long getIndexSize(final IndexContext<?> indexContext) {
		long indexSize = 0;
		try {
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
			if (latestIndexDirectory == null || !latestIndexDirectory.exists() || !latestIndexDirectory.isDirectory()) {
				return indexSize;
			}
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();
			if (serverIndexDirectories == null) {
				return indexSize;
			}
			for (File serverIndexDirectory : serverIndexDirectories) {
				if (serverIndexDirectory != null && serverIndexDirectory.exists() && serverIndexDirectory.isDirectory()) {
					File[] indexFiles = serverIndexDirectory.listFiles();
					if (indexFiles == null || indexFiles.length == 0) {
						continue;
					}
					for (File indexFile : indexFiles) {
						indexSize += indexFile.length();
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting the size of the index : " + this, e);
		}
		return indexSize;
	}

}