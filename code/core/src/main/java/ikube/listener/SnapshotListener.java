package ikube.listener;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleNotification(Event event) {
		if (Event.PERFORMANCE.equals(event.getType())) {
			Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
			for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
				IndexContext indexContext = mapEntry.getValue();
				Snapshot snapshot = new Snapshot();
				snapshot.setIndexSize(getIndexSize(indexContext));
				snapshot.setNumDocs(getNumDocs(indexContext));
				snapshot.setTimestamp(System.currentTimeMillis());
				snapshot.setLatestIndexTimestamp(getLatestIndexDirectoryDate(indexContext));
				snapshot.setDocsPerMinute(getDocsPerMinute(indexContext, snapshot));
				dataBase.persist(snapshot);
				// snapshot.setIndexContext(indexContext);
				indexContext.getSnapshots().add(snapshot);
				if (indexContext.getSnapshots().size() > IConstants.MAX_SNAPSHOTS) {
					List<Snapshot> subListToRemove = indexContext.getSnapshots().subList(0,
							(int) (((double) IConstants.MAX_SNAPSHOTS) * 0.25));
					LOGGER.info("Removing : " + subListToRemove.size());
					indexContext.getSnapshots().removeAll(subListToRemove);
				}
			}
		}
	}

	protected long getDocsPerMinute(final IndexContext<?> indexContext, final Snapshot snapshot) {
		List<Snapshot> snapshots = indexContext.getSnapshots();
		if (snapshots == null || snapshots.size() == 0) {
			return 0;
		}
		Snapshot previous = snapshots.get(snapshots.size() - 1);
		double interval = snapshot.getTimestamp() - previous.getTimestamp();
		double ratio = interval / 60000;
		// LOGGER.info("Ratio : " + ratio);
		long docsPerMinute = (long) ((snapshot.getNumDocs() - previous.getNumDocs()) / ratio);
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
			IndexWriter indexWriter = indexContext.getIndexWriter();
			MultiSearcher multiSearcher = indexContext.getMultiSearcher();
			if (indexWriter != null) {
				String[] files = indexWriter.getDirectory().listAll();
				for (String file : files) {
					indexSize += indexWriter.getDirectory().fileLength(file);
				}
			} else if (multiSearcher != null) {
				for (Searchable searchable : multiSearcher.getSearchables()) {
					String[] files = ((IndexSearcher) searchable).getIndexReader().directory().listAll();
					for (String file : files) {
						indexSize += ((IndexSearcher) searchable).getIndexReader().directory().fileLength(file);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting the size of the index : " + this, e);
		}
		return indexSize;
	}

}