package ikube.scheduling.schedule;

import ikube.action.index.IndexManager;
import ikube.cluster.IMonitorService;
import ikube.model.IndexContext;
import ikube.scheduling.Schedule;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

import org.apache.lucene.index.IndexWriter;
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
public class IndexSizeSchedule extends Schedule {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexSizeSchedule.class);

	@Value("${max.index.size}")
	private long maxIndexSize;
	@Autowired
	private IMonitorService monitorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void run() {
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext<?> indexContext = mapEntry.getValue();

			IndexWriter[] indexWriters = indexContext.getIndexWriters();
			if (indexWriters == null || indexWriters.length == 0) {
				continue;
			}

			long meg = 1024 * 1000;
			FSDirectory directory = (FSDirectory) indexWriters[indexWriters.length - 1].getDirectory();
			File indexDirectory = directory.getDirectory();
			long indexSize = IndexManager.getDirectorySize(indexDirectory);
			if (indexSize / meg < maxIndexSize) {
				continue;
			}
			boolean success = Boolean.TRUE;
			File newIndexDirectory = null;
			IndexWriter newIndexWriter = null;
			try {
				newIndexDirectory = getNewIndexDirectory(indexWriters);
				LOGGER.info("Starting new index : " + indexContext.getIndexName() + ", " + newIndexDirectory);
				newIndexWriter = IndexManager.openIndexWriter(indexContext, newIndexDirectory, true);

				IndexWriter[] newIndexWriters = new IndexWriter[indexWriters.length + 1];
				System.arraycopy(indexWriters, 0, newIndexWriters, 0, indexWriters.length);
				newIndexWriters[newIndexWriters.length - 1] = newIndexWriter;
				LOGGER.info("Switched to the new index writer : " + indexContext);
				indexContext.setIndexWriters(newIndexWriters);
				// We don't close the index writers here any more because they can still be used in the delta indexing. And
				// we close all the indexes in the context in the index manager at the end of the job
				// Note: the delta is not implemented so we close them here again
				IndexManager.closeIndexWriter(indexWriters[indexWriters.length]);
			} catch (Exception e) {
				LOGGER.error("Exception starting a new index : ", e);
				success = Boolean.FALSE;
			} finally {
				if (!success) {
					// Try to close and delete the new index writer and index directory
					IndexManager.closeIndexWriter(newIndexWriter);
					FileUtilities.deleteFile(newIndexDirectory, 1);
					// Remove the new index writer from the end of the array
					if (newIndexWriter != null) {
						indexWriters = indexContext.getIndexWriters();
						if (indexWriters[indexWriters.length - 1] == newIndexWriter) {
							IndexWriter[] newIndexWriters = new IndexWriter[indexWriters.length - 1];
							System.arraycopy(indexWriters, 0, newIndexWriters, 0, indexWriters.length - 1);
							indexContext.setIndexWriters(newIndexWriters);
						}
					}
				}
			}
		}
	}

	private File getNewIndexDirectory(final IndexWriter[] indexWriters) {
		// We take the first index directory and append a system time stamp to it
		FSDirectory directory = (FSDirectory) indexWriters[0].getDirectory();
		File indexDirectory = directory.getDirectory();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(indexDirectory.getAbsolutePath());
		stringBuilder.append(".");
		stringBuilder.append(Long.toString(System.currentTimeMillis()));
		File newIndexDirectory = FileUtilities.getFile(stringBuilder.toString(), Boolean.TRUE);

		return newIndexDirectory;
	}

}