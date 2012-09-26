package ikube.listener;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.Map;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * This listener will check the size of an index and start a new one if it gets over the maximum allowed size. This is because over NFS the
 * size of the Lucene index is important as there is a seek in the file, and aparently this is not well supported over NFS and the
 * performance degrades with the size of the index, i.e. over for example 10 gig.
 * 
 * @author Michael Couck
 * @since 29.08.12
 * @version 01.00
 */
public class IndexSizeListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexSizeListener.class);

	@Value("${max.index.size}")
	private long maxIndexSize;
	@Autowired
	private IClusterManager clusterManager;
	@Autowired
	private IMonitorService monitorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void handleNotification(Event event) {
		if (!Event.TIMER.equals(event.getType())) {
			return;
		}
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext<?> indexContext = mapEntry.getValue();
			IndexWriter indexWriter = indexContext.getIndexWriter();
			if (indexWriter == null) {
				continue;
			}
			long meg = 1024 * 1000;
			long indexSize = getIndexSize(mapEntry.getValue());
			// LOGGER.info("Index size : " + indexSize + ", " + maxIndexSize + ", " + (indexSize / meg < maxIndexSize));
			if (indexSize / meg < maxIndexSize) {
				continue;
			}
			FSDirectory directory = (FSDirectory) indexWriter.getDirectory();
			File indexDirectory = directory.getFile();
			try {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(indexDirectory.getParentFile().getAbsolutePath());
				stringBuilder.append(IConstants.SEP);
				stringBuilder.append(clusterManager.getServer().getAddress());
				stringBuilder.append(".");
				stringBuilder.append(Integer.valueOf(indexDirectory.getName().charAt(indexDirectory.getName().length() - 1)) + 1);
				File newIndexDirectory = FileUtilities.getFile(stringBuilder.toString(), Boolean.TRUE);
				LOGGER.info("Starting new index : " + newIndexDirectory);
				IndexWriter newIndexWriter = IndexManager.openIndexWriter(indexContext, newIndexDirectory, true);

				int retry = 10;
				boolean switched = false;
				while (retry-- > 0) {
					// Because this variable is volatile we can 'miss' a write
					// so we have to try a few times to be sure it is written to memory
					indexContext.setIndexWriter(newIndexWriter);
					if (indexContext.getIndexWriter() == newIndexWriter) {
						switched = true;
						break;
					}
					ThreadUtilities.sleep(100);
				}

				if (switched) {
					LOGGER.info("Switched to the new index writer : ");
					IndexManager.closeIndexWriter(indexWriter);
				} else {
					LOGGER.warn("Didn't switch to the new index writer, will try again next notification : ");
				}
			} catch (Exception e) {
				LOGGER.error("Exception starting a new index : ", e);
			}
		}
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
					Directory directory = null;
					try {
						directory = FSDirectory.open(serverIndexDirectory);
						if (!IndexWriter.isLocked(directory)) {
							// We are looking for the locked directory, i.e. the one that is being written to
							continue;
						}
						File[] indexFiles = serverIndexDirectory.listFiles();
						if (indexFiles == null || indexFiles.length == 0) {
							continue;
						}
						for (File indexFile : indexFiles) {
							indexSize += indexFile.length();
						}
						break;
					} finally {
						try {
							if (directory != null) {
								directory.close();
							}
						} catch (Exception e) {
							LOGGER.error("Exception closing the directory : ", e);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting the size of the index : " + this, e);
		}
		return indexSize;
	}

}