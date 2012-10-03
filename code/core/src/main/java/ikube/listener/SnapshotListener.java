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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
			OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
			Server server = clusterManager.getServer();
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

				Snapshot snapshot = new Snapshot();
				// LOGGER.info("Snapshot : " + snapshot);
				dataBase.persist(snapshot);

				snapshot.setIndexSize(getIndexSize(indexContext));
				snapshot.setNumDocs(getNumDocs(indexContext));
				snapshot.setLatestIndexTimestamp(getLatestIndexDirectoryDate(indexContext));
				snapshot.setDocsPerMinute(getDocsPerMinute(indexContext, snapshot));
				snapshot.setTotalSearches(getTotalSearchesForIndex(indexContext));
				// LOGGER.info("Setting searches per minute : " + getSearchesPerMinute(indexContext, snapshot));
				snapshot.setSearchesPerMinute(getSearchesPerMinute(indexContext, snapshot));

				dataBase.merge(snapshot);
				// LOGGER.info("Snapshot : " + snapshot);
				indexContext.getSnapshots().add(snapshot);
				if (indexContext.getSnapshots().size() > IConstants.MAX_SNAPSHOTS) {
					List<Snapshot> snapshots = new ArrayList<Snapshot>(indexContext.getSnapshots());
					List<Snapshot> subListToRemove = snapshots.subList(0, (int) (((double) IConstants.MAX_SNAPSHOTS) * 0.25));
					snapshots.removeAll(subListToRemove);
					indexContext.setSnapshots(snapshots);
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
		// LOGGER.info("Previous : " + previous.getTimestamp() + ", " + snapshot.getTimestamp());
		double interval = snapshot.getTimestamp().getTime() - previous.getTimestamp().getTime();
		double ratio = interval / 60000;
		long docsPerMinute = (long) ((snapshot.getNumDocs() - previous.getNumDocs()) / ratio);
		if (docsPerMinute < 0) {
			docsPerMinute = 0;
		}
		return docsPerMinute < 50000 ? docsPerMinute : 0;
	}

	protected Date getLatestIndexDirectoryDate(final IndexContext<?> indexContext) {
		long timestamp = 0;
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
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
		if (indexWriter != null) {
			try {
				numDocs += indexWriter.numDocs();
			} catch (IOException e) {
				LOGGER.error("Exception reading the number of documents from the writer", e);
			}
			File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
			if (latestIndexDirectory != null) {
				File[] serverIndexDirectories = latestIndexDirectory.listFiles();
				if (serverIndexDirectories != null) {
					Directory directory = null;
					IndexReader indexReader = null;
					for (File serverIndexDirectory : serverIndexDirectories) {
						try {
							directory = FSDirectory.open(serverIndexDirectory);
							if (!IndexWriter.isLocked(directory)) {
								indexReader = IndexReader.open(directory);
								numDocs += indexReader.numDocs();
							};
						} catch (Exception e) {
							LOGGER.error("Exception opening the reader on the index : " + serverIndexDirectory, e);
						} finally {
							try {
								if (directory != null) {
									directory.close();
								}
								if (indexReader != null) {
									indexReader.close();
								}
							} catch (Exception e) {
								LOGGER.error("Exception closing the readon on the index : ", e);
							}
						}
					}
				}
			}
		} else {
			MultiSearcher multiSearcher = indexContext.getMultiSearcher();
			if (multiSearcher != null) {
				for (Searchable searchable : multiSearcher.getSearchables()) {
					numDocs += ((IndexSearcher) searchable).getIndexReader().numDocs();
				}
			}
		}
		return numDocs;
	}

	protected long getIndexSize(final IndexContext<?> indexContext) {
		long indexSize = 0;
		try {
			File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
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